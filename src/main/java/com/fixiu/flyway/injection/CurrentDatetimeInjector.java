package com.fixiu.flyway.injection;

import java.sql.Timestamp;
import java.time.Instant;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimestampValue;

/**
 * Current date time will be set
 */
public class CurrentDatetimeInjector extends AbstractValueInjector{
	@Override
	public Expression getValue() {
		return new TimestampValue().withValue(Timestamp.from(Instant.now()));
	}

}
