package indi.mybatis.flying.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DatabaseTearDowns;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;

import indi.mybatis.flying.pagination.Page;
import indi.mybatis.flying.pagination.PageParam;
import indi.mybatis.flying.pojo.Account2_;
import indi.mybatis.flying.pojo.Account_;
import indi.mybatis.flying.pojo.Detail_;
import indi.mybatis.flying.pojo.LoginLog_;
import indi.mybatis.flying.pojo.Role2_;
import indi.mybatis.flying.pojo.Role_;
import indi.mybatis.flying.pojo.condition.Account_Condition;
import indi.mybatis.flying.service.AccountService;
import indi.mybatis.flying.service.DetailService;
import indi.mybatis.flying.service.LoginLogService;
import indi.mybatis.flying.service.RoleService;
import indi.mybatis.flying.service2.Account2Service;
import indi.mybatis.flying.service2.Role2Service;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DbUnitConfiguration(dataSetLoader = FlatXmlDataSetLoader.class, databaseConnection = { "dataSource1", "dataSource2" })
@ContextConfiguration("classpath:spring-test.xml")
public class CacheTest {

	@Autowired
	private AccountService accountService;

	@Autowired
	private DetailService detailService;

	@Autowired
	private LoginLogService loginLogService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Account2Service account2Service;

	@Autowired
	private Role2Service role2Service;

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetup(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/test.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test.result.xml")
	public void test() {
		String name = "ann";
		String newName = "bob";
		String loginIP = "0.0.0.1";

		Account_ a = new Account_();
		LoginLog_ l = new LoginLog_();

		a.setName(name);
		accountService.insert(a);

		l.setLoginIP(loginIP);
		l.setAccount(a);
		loginLogService.insert(l);

		LoginLog_ loginLog = loginLogService.select(l.getId());
		Assert.assertEquals(name, loginLog.getAccount().getName());
		Account_ account = accountService.select(a.getId());
		account.setName(newName);
		accountService.update(account);
		LoginLog_ loginLog2 = loginLogService.select(l.getId());
		Assert.assertEquals(newName, loginLog2.getAccount().getName());
	}

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetup(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test2.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/test2.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test2.result.xml")
	public void test2() {
		String name = "新权限", newName = "新角色", accountName = "ann", ip = "0.0.0.1";
		Role_ r = new Role_();
		r.setName(name);
		roleService.insert(r);

		Account_ a = new Account_();
		a.setName(accountName);
		a.setRole(r);
		accountService.insert(a);

		LoginLog_ l = new LoginLog_();
		l.setLoginIP(ip);
		l.setAccount(a);
		loginLogService.insert(l);

		Account_ account = accountService.select(a.getId());
		Assert.assertEquals(name, account.getRole().getName());

		LoginLog_ loginLog = loginLogService.select(l.getId());
		Assert.assertEquals(name, loginLog.getAccount().getRole().getName());

		Role_ role = roleService.select(r.getId());
		role.setName(newName);
		roleService.update(role);

		Account_ account2 = accountService.select(a.getId());
		Assert.assertEquals(newName, account2.getRole().getName());

		LoginLog_ loginLog2 = loginLogService.select(l.getId());
		Assert.assertEquals(newName, loginLog2.getAccount().getRole().getName());
	}

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetup(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test3.xml")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/test3.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/test3.result.xml")
	public void test3() {
		String name = "新权限", newName = "新角色", newName2 = "新新角色", accountName = "ann", ip = "0.0.0.1", detailName = "细节";
		Role_ r = new Role_();
		r.setName(name);
		roleService.insert(r);

		Account_ a = new Account_();
		a.setName(accountName);
		a.setRole(r);
		accountService.insert(a);

		LoginLog_ l = new LoginLog_();
		l.setLoginIP(ip);
		l.setAccount(a);
		loginLogService.insert(l);

		Detail_ d = new Detail_();
		d.setName(detailName);
		d.setLoginLog(l);
		detailService.insert(d);

		Account_ account = accountService.select(a.getId());
		Assert.assertEquals(name, account.getRole().getName());

		LoginLog_ loginLog = loginLogService.select(l.getId());
		Assert.assertEquals(name, loginLog.getAccount().getRole().getName());

		Role_ role = roleService.select(r.getId());
		role.setName(newName);
		roleService.update(role);

		Account_ account2 = accountService.select(a.getId());
		Assert.assertEquals(newName, account2.getRole().getName());

		LoginLog_ loginLog2 = loginLogService.select(l.getId());
		Assert.assertEquals(newName, loginLog2.getAccount().getRole().getName());

		Detail_ detail = detailService.select(d.getId());
		Assert.assertEquals(accountName, detail.getLoginLog().getAccount().getName());
		Assert.assertEquals(newName, detail.getLoginLog().getAccount().getRole().getName());

		// Account_ account3 = accountService.select(a.getId());
		// account3.setName(newAccountName);
		// accountService.update(account3);

		Role_ role2 = roleService.select(r.getId());
		role2.setName(newName2);
		roleService.update(role2);

		Detail_ detail2 = detailService.select(d.getId());
		Assert.assertEquals(newName2, detail2.getLoginLog().getAccount().getRole().getName());
	}

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetup(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testPagination.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testPagination.result.xml")
	public void testPagination() {
		String name1 = "ann", name2 = "bob", name3 = "carl", name4 = "duke";

		Account_ a1 = new Account_();
		a1.setId(1);
		a1.setName(name1);
		accountService.insert(a1);

		Account_ a2 = new Account_();
		a2.setId(2);
		a2.setName(name2);
		accountService.insert(a2);

		Account_ a3 = new Account_();
		a3.setId(3);
		a3.setName(name3);
		accountService.insert(a3);

		Account_ a4 = new Account_();
		a4.setId(4);
		a4.setName(name4);
		accountService.insert(a4);

		Account_Condition ac = new Account_Condition();
		ac.setLimiter(new PageParam(1, 2));
		Collection<Account_> c1 = accountService.selectAll(ac);
		Assert.assertEquals(2, c1.size());
		for (Account_ a : c1) {
			if (a.getId() == 1) {
				Assert.assertEquals(name1, a.getName());
			} else {
				Assert.assertEquals(name2, a.getName());
			}
		}
		Assert.assertEquals(2, ac.getLimiter().getMaxPageNum());

		ac.setLimiter(new PageParam(1, 2));
		c1 = accountService.selectAll(ac);
		Assert.assertEquals(2, ac.getLimiter().getMaxPageNum());

		accountService.delete(a1);
		accountService.delete(a2);
		accountService.delete(a3);
		accountService.delete(a4);
	}

	/* 测试分页缓存的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testClearCache.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testClearCache.result.xml")
	public void testClearCache() {
		Role_ r = new Role_(), r2 = new Role_();

		r.setId(1);
		r.setName("root");
		roleService.insert(r);

		r2.setId(2);
		r2.setName("deployer");
		roleService.insert(r2);

		Account_ a = new Account_(), a2 = new Account_(), a3 = new Account_(), a4 = new Account_(), a5 = new Account_(),
				a6 = new Account_(), a7 = new Account_(), a8 = new Account_(), a9 = new Account_(),
				a10 = new Account_(), a11 = new Account_(), a12 = new Account_();

		a.setName("ann");
		accountService.insert(a);

		a2.setName("bob");
		accountService.insert(a2);

		a3.setName("caq");
		accountService.insert(a3);

		a4.setName("don");
		accountService.insert(a4);

		a5.setName("eli");
		accountService.insert(a5);

		a6.setName("fea");
		accountService.insert(a6);

		a7.setName("gus");
		accountService.insert(a7);

		a8.setName("hex");
		accountService.insert(a8);

		a9.setName("ivy");
		accountService.insert(a9);

		a10.setName("jak");
		accountService.insert(a10);

		a11.setName("kir");
		a11.setRole(r);
		accountService.insert(a11);

		a12.setName("lee");
		a12.setRole(r);
		accountService.insert(a12);

		Account_Condition ac = new Account_Condition();
		ac.setLimiter(new PageParam(1, 10));
		Collection<Account_> c = accountService.selectAll(ac);
		Page<Account_> p = new Page<>(c, ac.getLimiter());
		Assert.assertEquals(2, p.getMaxPageNum());
		Assert.assertEquals(12, p.getTotalCount());
		Assert.assertEquals(1, p.getPageNo());
		Assert.assertEquals(10, p.getPageItems().size());

		Account_Condition ac2 = new Account_Condition();
		ac2.setLimiter(new PageParam(2, 10));
		Collection<Account_> c2 = accountService.selectAll(ac2);
		Page<Account_> p2 = new Page<>(c2, ac2.getLimiter());
		Assert.assertEquals(2, p2.getMaxPageNum());
		Assert.assertEquals(12, p2.getTotalCount());
		Assert.assertEquals(2, p2.getPageNo());
		Assert.assertEquals(2, p2.getPageItems().size());
		for (Account_ temp : p2.getPageItems()) {
			Assert.assertEquals("root", temp.getRole().getName());
		}

		a11.setRole(r2);
		accountService.update(a11);

		a12.setRole(r2);
		accountService.update(a12);

		Account_Condition ac3 = new Account_Condition();
		ac3.setLimiter(new PageParam(2, 10));
		Collection<Account_> c3 = accountService.selectAll(ac3);
		Page<Account_> p3 = new Page<>(c3, ac3.getLimiter());
		Assert.assertEquals(2, p3.getMaxPageNum());
		Assert.assertEquals(12, p3.getTotalCount());
		Assert.assertEquals(2, p3.getPageNo());
		Assert.assertEquals(2, p3.getPageItems().size());
		for (Account_ temp : p3.getPageItems()) {
			Assert.assertEquals("deployer", temp.getRole().getName());
		}
	}

	/* 测试分页缓存能正确清除父对象的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testClearCache2.result.xml")
	@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testClearCache2.result.xml")
	public void testClearCache2() {
		Role_ r = new Role_(), r2 = new Role_();

		r.setId(1);
		r.setName("root");
		roleService.insert(r);

		r2.setId(2);
		r2.setName("deployer");
		roleService.insert(r2);

		Account_ a = new Account_(), a2 = new Account_(), a3 = new Account_(), a4 = new Account_(), a5 = new Account_(),
				a6 = new Account_(), a7 = new Account_(), a8 = new Account_(), a9 = new Account_(),
				a10 = new Account_(), a11 = new Account_(), a12 = new Account_();

		a.setName("ann");
		a.setEmail("");
		a.setRole(r);
		accountService.insert(a);

		a2.setName("bob");
		a2.setEmail("");
		a2.setRole(r);
		accountService.insert(a2);

		a3.setName("caq");
		a3.setEmail("");
		a3.setRole(r);
		accountService.insert(a3);

		a4.setName("don");
		a4.setEmail("");
		a4.setRole(r);
		accountService.insert(a4);

		a5.setName("eli");
		a5.setEmail("");
		a5.setRole(r);
		accountService.insert(a5);

		a6.setName("fea");
		a6.setEmail("");
		a6.setRole(r);
		accountService.insert(a6);

		a7.setName("gus");
		a7.setEmail("");
		a7.setRole(r);
		accountService.insert(a7);

		a8.setName("hex");
		a8.setEmail("");
		a8.setRole(r);
		accountService.insert(a8);

		a9.setName("ivy");
		a9.setEmail("");
		a9.setRole(r);
		accountService.insert(a9);

		a10.setName("jak");
		a10.setEmail("");
		a10.setRole(r);
		accountService.insert(a10);

		a11.setName("kir");
		a11.setEmail("");
		a11.setRole(r);
		accountService.insert(a11);

		a12.setName("lee");
		a12.setEmail("");
		a12.setRole(r);
		accountService.insert(a12);

		Role_ role = roleService.select(1);

		Account_Condition ac = new Account_Condition();
		ac.setLimiter(new PageParam(2, 10));
		ac.setEmail("");
		Role_ rc = new Role_();
		rc.setId(1);
		ac.setRole(rc);
		Collection<Account_> c = accountService.selectAll(ac);
		Page<Account_> p = new Page<>(c, ac.getLimiter());
		Assert.assertEquals(2, p.getPageItems().size());
		for (Account_ temp : p.getPageItems()) {
			Assert.assertEquals("root", temp.getRole().getName());
		}

		Account_Condition ac1 = new Account_Condition();
		ac1.setLimiter(new PageParam(2, 10));
		ac1.setEmail("");
		Role_ rc1 = new Role_();
		rc1.setId(1);
		ac1.setRole(rc1);
		Collection<Account_> c1 = accountService.selectAll(ac1);

		role.setName("rootNew");
		roleService.update(role);

		Account_Condition ac2 = new Account_Condition();
		ac2.setLimiter(new PageParam(2, 10));
		ac2.setEmail("");
		Role_ rc2 = new Role_();
		rc2.setId(1);
		ac2.setRole(rc2);
		Collection<Account_> c2 = accountService.selectAll(ac2);
		Page<Account_> p2 = new Page<>(c2, ac2.getLimiter());
		Assert.assertEquals(2, p2.getPageItems().size());
		for (Account_ temp : p2.getPageItems()) {
			Assert.assertEquals("rootNew", temp.getRole().getName());
		}
	}

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetup(connection = "dataSource2", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test.datasource2.xml")
	@ExpectedDatabase(connection = "dataSource2", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest2/test.datasource2.result.xml")
	@DatabaseTearDown(connection = "dataSource2", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test.datasource2.result.xml")
	public void test21() {
		String name = "ann";
		String roleName = "user";
		String newRoleName = "admin";

		Account2_ a = new Account2_();
		Role2_ r = new Role2_();

		r.setName(roleName);
		role2Service.insert(r);

		a.setName(name);
		a.setRole(r);
		account2Service.insert(a);

		Account2_ account2_ = account2Service.select(a.getId());
		Assert.assertEquals(roleName, account2_.getRole().getName());

		Role2_ r2 = role2Service.select(r.getId());
		r2.setName(newRoleName);
		role2Service.update(r2);

		account2_ = account2Service.select(a.getId());
		Assert.assertEquals(newRoleName, account2_.getRole().getName());
	}

	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@DatabaseSetups({
			@DatabaseSetup(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource1.xml"),
			@DatabaseSetup(connection = "dataSource2", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource2.xml") })
	@ExpectedDatabases({
			@ExpectedDatabase(connection = "dataSource1", override = false, assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource1.result.xml"),
			@ExpectedDatabase(connection = "dataSource2", override = false, assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource2.result.xml") })
	@DatabaseTearDowns({
			@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource1.result.xml"),
			@DatabaseTearDown(connection = "dataSource2", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest2/test2.datasource2.result.xml") })
	public void test22() {
		String name = "ann";
		String roleName = "user";
		String newRoleName = "admin";

		Account_ a = new Account_();
		Role_ r = new Role_();

		r.setName(roleName);
		roleService.insert(r);

		a.setName(name);
		a.setRole(r);
		accountService.insert(a);

		Account_ account_ = accountService.select(a.getId());
		Assert.assertEquals(roleName, account_.getRole().getName());

		Role_ r12 = roleService.select(r.getId());
		r12.setName(newRoleName);
		roleService.update(r12);

		account_ = accountService.select(a.getId());
		Assert.assertEquals(newRoleName, account_.getRole().getName());

		Account2_ a2 = new Account2_();
		Role2_ r2 = new Role2_();

		r2.setName(roleName);
		role2Service.insert(r2);

		a2.setName(name);
		a2.setRole(r2);
		account2Service.insert(a2);

		Account2_ account2_ = account2Service.select(a2.getId());
		Assert.assertEquals(roleName, account2_.getRole().getName());

		Role2_ r22 = role2Service.select(r2.getId());
		r22.setName(newRoleName);
		role2Service.update(r22);

		account2_ = account2Service.select(a2.getId());
		Assert.assertEquals(newRoleName, account2_.getRole().getName());
	}

	/* 测试在select查询的情况下，缓存确实生效的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	public void testUpdateDirect() {
		Role_ r = new Role_();
		r.setId(1);
		r.setName("ann");
		roleService.insert(r);
		Account_ a = new Account_();
		a.setId(1);
		a.setRole(r);
		a.setEmail("email");
		accountService.insert(a);

		Account_ account = accountService.select(1);
		Assert.assertEquals("ann", account.getRole().getName());

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "bob");
		roleService.updateDirect(m);

		Account_ account2 = accountService.select(1);
		Assert.assertEquals("ann", account2.getRole().getName());
	}

	/* 测试在selectAll查询的情况下，缓存确实生效的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	public void testUpdateDirect2() {
		Role_ r = new Role_();
		r.setId(1);
		r.setName("ann");
		roleService.insert(r);
		Account_ a = new Account_();
		a.setId(1);
		a.setRole(r);
		a.setEmail("email");
		accountService.insert(a);

		Account_ ac = new Account_();
		ac.setEmail("email");
		Collection<Account_> c = accountService.selectAll(ac);

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "bob");
		roleService.updateDirect(m);

		Account_ ac2 = new Account_();
		ac2.setEmail("email");
		Collection<Account_> c2 = accountService.selectAll(ac2);
		for (Account_ t : c2) {
			Assert.assertEquals("ann", t.getRole().getName());
		}
	}

	/* 测试在查询对象查询的情况下，缓存确实生效的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	public void testUpdateDirect3() {
		Role_ r = new Role_();
		r.setId(1);
		r.setName("ann");
		roleService.insert(r);
		Account_ a = new Account_();
		a.setId(1);
		a.setRole(r);
		a.setEmail("email");
		accountService.insert(a);

		Account_Condition ac = new Account_Condition();
		ac.setEmailLike("mai");
		Collection<Account_> c = accountService.selectAll(ac);

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "bob");
		roleService.updateDirect(m);

		Account_Condition ac2 = new Account_Condition();
		ac2.setEmailLike("mai");
		Collection<Account_> c2 = accountService.selectAll(ac2);
		for (Account_ t : c2) {
			Assert.assertEquals("ann", t.getRole().getName());
		}
	}

	/* 测试在查询对象查询的情况下，缓存确实生效的用例 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testUpdateDirect.result.xml")
	public void testUpdateDirect4() {
		Role_ r = new Role_();
		r.setId(1);
		r.setName("ann");
		roleService.insert(r);
		Account_ a = new Account_();
		a.setId(1);
		a.setRole(r);
		a.setEmail("email");
		accountService.insert(a);

		Account_Condition ac = new Account_Condition();
		ac.setLimiter(new PageParam(1, 1));
		Collection<Account_> c = accountService.selectAll(ac);

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "bob");
		roleService.updateDirect(m);

		Account_Condition ac2 = new Account_Condition();
		ac2.setLimiter(new PageParam(1, 1));
		Collection<Account_> c2 = accountService.selectAll(ac2);
		for (Account_ t : c2) {
			Assert.assertEquals("ann", t.getRole().getName());
		}
	}

	/*
	 * 设计两个注入值完全相同的同一pojo的select，观察它们是否共享缓存。 方法：先证明缓存生成之前updateDirect可以影响缓存，
	 * 再证明缓存生成之后updateDirect不能影响缓存，再看注入值相同的另一个select的结果是否受updateDirect影响。
	 * 结论：不共享缓存。
	 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testSameInjection.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testSameInjection.result.xml")
	public void testSameInjection() {
		Role_ r = new Role_(), r2 = new Role_();

		r.setId(1);
		r.setName("root");
		roleService.insert(r);

		r2.setId(2);
		r2.setName("deployer");
		roleService.insert(r2);

		Role_ role = roleService.select(1);
		Assert.assertEquals("root", role.getName());

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "newRoot");
		roleService.updateDirect(m);

		Role_ role2 = roleService.select(1);
		Assert.assertEquals("root", role2.getName());

		Role_ role3 = roleService.selectEverything(1);
		Assert.assertEquals("newRoot", role3.getName());
	}

	/* 设计两个注入值中只有ignoreTag不同的同一pojo的select，可以同时正常缓存失效 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testNearlySameInjection.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testNearlySameInjection.result.xml")
	public void testNearlySameInjection() {
		Role_ r = new Role_(), r2 = new Role_();

		r.setId(1);
		r.setName("root");
		roleService.insert(r);

		r2.setId(2);
		r2.setName("deployer");
		roleService.insert(r2);

		Role_ role = roleService.select(1);
		Assert.assertEquals("root", role.getName());

		Role_ role2 = roleService.selectNoId(1);
		Assert.assertEquals("root", role2.getName());
		Assert.assertNull(role2.getId());

		r.setName("newRoot");
		roleService.update(r);

		Role_ role3 = roleService.select(1);
		Assert.assertEquals("newRoot", role3.getName());

		Role_ role4 = roleService.selectNoId(1);
		Assert.assertEquals("newRoot", role4.getName());
		Assert.assertNull(role4.getId());
	}

	/* 两个相同注入值的不同pojo的select，使其中一个缓存失效，不会影响到另一个。 */
	@Test
	@IfProfileValue(name = "CACHE", value = "true")
	@ExpectedDatabase(connection = "dataSource1", assertionMode = DatabaseAssertionMode.NON_STRICT, value = "/indi/mybatis/flying/test/cacheTest/testSameIdAndInjectionInDifferentPojos.result.xml")
	@DatabaseTearDown(connection = "dataSource1", type = DatabaseOperation.DELETE_ALL, value = "/indi/mybatis/flying/test/cacheTest/testSameIdAndInjectionInDifferentPojos.result.xml")
	public void testSameIdAndInjectionInDifferentPojos() {
		Role_ r = new Role_();
		r.setId(1);
		r.setName("root");
		roleService.insert(r);

		Account_ a = new Account_();
		a.setId(1);
		a.setName("deployer");
		accountService.insert(a);

		Role_ role = roleService.selectEverything(1);
		Account_ account = accountService.selectEverything(1);

		a.setName("newDeployer");
		accountService.update(a);

		Account_ account2 = accountService.selectEverything(1);
		Assert.assertEquals("newDeployer", account2.getName());

		Map<String, Object> m = new HashMap<>();
		m.put("id", 1);
		m.put("name", "newRoot");
		roleService.updateDirect(m);

		Role_ role2 = roleService.selectEverything(1);
		Assert.assertEquals("root", role2.getName());
	}
}
