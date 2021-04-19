package com.fixiu.flyway.injection;

import java.sql.Time;
import java.time.LocalTime;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimeValue;

/**
 * Current time will be set
 */
public class CurrentTimeInjector extends AbstractValueInjector{
	@Override
	public Expression getValue() {
		return new TimeValue().withValue(Time.valueOf(LocalTime.now()));
	}

}
