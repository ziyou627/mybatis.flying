package indi.mybatis.flying.interceptors;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import indi.mybatis.flying.models.Conditionable;
import indi.mybatis.flying.models.Limitable;
import indi.mybatis.flying.models.Sortable;

/**
 * 采用分页拦截器执行物理分页查询时，原生的Executor创建cacheKey时未能包含分页参数page，为了解决这个问题，创建了本拦截器，
 * 本拦截器会拦截CachingExecutor的query方法，在创建cacheKey时将分页参数page包含其中。 老规矩，签名里要拦截的类型只能是接口。
 * 
 * @author 湖畔微风
 * 
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
		RowBounds.class, ResultHandler.class }) })
public class PaginationFixInteceptor implements Interceptor {
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

	private static final String H = "h";
	private static final String TARGET = "target";

	private static final String DIRTY = "dirty";
	private static final String LIMITER = "limiter";
	private static final String LIST = "list";
	private static final String DELEGATE = "delegate";
	private static final String TCM = "tcm";

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Executor executorProxy = (Executor) invocation.getTarget();
		MetaObject metaExecutor = MetaObject.forObject(executorProxy, DEFAULT_OBJECT_FACTORY,
				DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		/* 分离代理对象链 */
		while (metaExecutor.hasGetter(H)) {
			Object object = metaExecutor.getValue(H);
			metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,
					DEFAULT_REFLECTOR_FACTORY);
		}
		/* 分离最后一个代理对象的目标类 */
		while (metaExecutor.hasGetter(TARGET)) {
			Object object = metaExecutor.getValue(TARGET);
			metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,
					DEFAULT_REFLECTOR_FACTORY);
		}
		Object[] args = invocation.getArgs();
		return this.query(metaExecutor, args);
	}

	private <E> List<E> query(MetaObject metaExecutor, Object[] args) throws SQLException {
		MappedStatement mappedStatement = (MappedStatement) args[0];
		Object parameter = args[1];
		RowBounds rowBounds = (RowBounds) args[2];
		ResultHandler resultHandler = (ResultHandler) args[3];
		BoundSql boundSql = mappedStatement.getBoundSql(parameter);
		CacheKey cacheKey = createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
		return this.query(metaExecutor, mappedStatement, cacheKey, parameter, rowBounds, resultHandler, boundSql);
	}

	@SuppressWarnings("unchecked")
	private <E> List<E> query(MetaObject metaExecutor, MappedStatement mappedStatement, CacheKey cacheKey,
			Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		MetaObject metaParameter = MetaObject.forObject(parameter, DEFAULT_OBJECT_FACTORY,
				DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		/* 当需要分页查询时，缓存里加入page信息 */
		if (metaParameter.getOriginalObject() instanceof Conditionable) {
			Cache cache = mappedStatement.getCache();
			if (cache != null) {
				if (mappedStatement.isUseCache() && resultHandler == null) {
					if (!(Boolean) metaExecutor.getValue(DIRTY)) {
						cache.getReadWriteLock().readLock().lock();
						try {
							synchronized (cache) {
								Object value = cache.getObject(cacheKey);
								if (value != null) {
									HashMap<String, Object> cachedMap = (HashMap<String, Object>) value;
									Limitable cachedPage = (Limitable) cachedMap.get(LIMITER);
									Limitable originalPage = ((Conditionable) metaParameter.getOriginalObject())
											.getLimiter();
									if (null != originalPage && null != cachedPage) {
										originalPage.setTotalCount(cachedPage.getTotalCount());
										return (List<E>) cachedMap.get(LIST);
									}
								}
							}
						} finally {
							cache.getReadWriteLock().readLock().unlock();
						}
					}
				}
				Executor delegate = (Executor) metaExecutor.getValue(DELEGATE);
				List<E> list = delegate.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
				TransactionalCacheManager tcm = (TransactionalCacheManager) metaExecutor.getValue(TCM);
				HashMap<String, Object> cachedMap = new HashMap<String, Object>();
				cachedMap.put(LIMITER, metaParameter.getValue(LIMITER));
				cachedMap.put(LIST, list);
				tcm.putObject(cache, cacheKey, cachedMap);
				return list;
			}
		}
		Executor executor = (Executor) metaExecutor.getOriginalObject();
		return executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
	}

	private CacheKey createCacheKey(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds,
			BoundSql boundSql) {
		CacheKey cacheKey = new CacheKey();
		cacheKey.update(mappedStatement.getId());
		cacheKey.update(rowBounds.getOffset());
		cacheKey.update(rowBounds.getLimit());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		cacheKey.update(boundSql.getSql());
		MetaObject metaObject = MetaObject.forObject(parameter, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,
				DEFAULT_REFLECTOR_FACTORY);

		if (parameterMappings.size() > 0 && parameter != null) {
			TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
			if (typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
				cacheKey.update(parameter);
			} else {
				for (ParameterMapping parameterMapping : parameterMappings) {
					String propertyName = parameterMapping.getProperty();
					if (metaObject.hasGetter(propertyName)) {
						cacheKey.update(metaObject.getValue(propertyName));
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						cacheKey.update(boundSql.getAdditionalParameter(propertyName));
					}
				}
			}
		}
		/* 当需要分页查询时，将page参数里的当前页和每页数加到cachekey里 */
		if (metaObject.getOriginalObject() instanceof Conditionable) {
			Sortable sorter = ((Conditionable) metaObject.getOriginalObject()).getSorter();
			if (sorter != null) {
				cacheKey.update(sorter.toSql());
			}
			Limitable limiter = ((Conditionable) metaObject.getOriginalObject()).getLimiter();
			if (limiter != null) {
				cacheKey.update(limiter.getPageNo());
				cacheKey.update(limiter.getPageSize());
			}
		}
		return cacheKey;
	}

	/**
	 * 只拦截CachingExecutor，其他的直接返回目标本身
	 */
	@Override
	public Object plugin(Object target) {
		if (target instanceof CachingExecutor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
