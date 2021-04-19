package com.fixiu.flyway.injection;

import net.sf.jsqlparser.expression.Expression;

/**
 * Custom value will be set
 */
public abstract class AbstractValueInjector {

	public abstract Expression getValue();
}
