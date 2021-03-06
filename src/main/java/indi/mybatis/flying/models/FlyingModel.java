package indi.mybatis.flying.models;

import indi.mybatis.flying.statics.ActionType;
import indi.mybatis.flying.statics.KeyGeneratorType;
import indi.mybatis.flying.type.KeyHandler;

public class FlyingModel {
	private boolean hasFlyingFeature;
	private ActionType actionType;
	private String ignoreTag;
	private KeyGeneratorType keyGeneratorType;
	private KeyHandler keyHandler;

	public boolean isHasFlyingFeature() {
		return hasFlyingFeature;
	}

	public void setHasFlyingFeature(boolean hasFlyingFeature) {
		this.hasFlyingFeature = hasFlyingFeature;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getIgnoreTag() {
		return ignoreTag;
	}

	public void setIgnoreTag(String ignoreTag) {
		this.ignoreTag = ignoreTag;
	}

	public KeyGeneratorType getKeyGeneratorType() {
		return keyGeneratorType;
	}

	public void setKeyGeneratorType(KeyGeneratorType keyGeneratorType) {
		this.keyGeneratorType = keyGeneratorType;
	}

	public KeyHandler getKeyHandler() {
		return keyHandler;
	}

	public void setKeyHandler(KeyHandler keyHandler) {
		this.keyHandler = keyHandler;
	}

}