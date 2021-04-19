package com.fixiu.flyway.injection;

import java.time.LocalDate;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;

/**
 * Current date will be set
 */
public class CurrentDateInjector extends AbstractValueInjector{
	@Override
	public Expression getValue() {
		return new DateValue().withValue(java.sql.Date.valueOf(LocalDate.now()));
	}

}
