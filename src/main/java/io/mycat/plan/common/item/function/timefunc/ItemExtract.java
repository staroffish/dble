/**
 * 
 */
package io.mycat.plan.common.item.function.timefunc;

import java.math.BigInteger;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalUnit;

import io.mycat.plan.common.field.Field;
import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.primary.ItemIntFunc;
import io.mycat.plan.common.ptr.LongPtr;
import io.mycat.plan.common.time.MySQLTime;
import io.mycat.plan.common.time.MyTime;

public class ItemExtract extends ItemIntFunc {
	private MySqlIntervalUnit int_type;
	private boolean date_value;

	public ItemExtract(Item a, MySqlIntervalUnit int_type) {
		super(a);
		this.int_type = int_type;
	}

	@Override
	public final String funcName() {
		return "extract";
	}

	@Override
	public Functype functype() {
		return Functype.EXTRACT_FUNC;
	}

	@Override
	public void fixLengthAndDec() {
		maybeNull = true; // If wrong date
		switch (int_type) {
		case YEAR:
			maxLength = 4;
			date_value = true;
			break;
		case YEAR_MONTH:
			maxLength = 6;
			date_value = true;
			break;
		case QUARTER:
			maxLength = 2;
			date_value = true;
			break;
		case MONTH:
			maxLength = 2;
			date_value = true;
			break;
		case WEEK:
			maxLength = 2;
			date_value = true;
			break;
		case DAY:
			maxLength = 2;
			date_value = true;
			break;
		case DAY_HOUR:
			maxLength = 9;
			date_value = false;
			break;
		case DAY_MINUTE:
			maxLength = 11;
			date_value = false;
			break;
		case DAY_SECOND:
			maxLength = 13;
			date_value = false;
			break;
		case HOUR:
			maxLength = 2;
			date_value = false;
			break;
		case HOUR_MINUTE:
			maxLength = 4;
			date_value = false;
			break;
		case HOUR_SECOND:
			maxLength = 6;
			date_value = false;
			break;
		case MINUTE:
			maxLength = 2;
			date_value = false;
			break;
		case MINUTE_SECOND:
			maxLength = 4;
			date_value = false;
			break;
		case SECOND:
			maxLength = 2;
			date_value = false;
			break;
		case MICROSECOND:
			maxLength = 2;
			date_value = false;
			break;
		case DAY_MICROSECOND:
			maxLength = 20;
			date_value = false;
			break;
		case HOUR_MICROSECOND:
			maxLength = 13;
			date_value = false;
			break;
		case MINUTE_MICROSECOND:
			maxLength = 11;
			date_value = false;
			break;
		case SECOND_MICROSECOND:
			maxLength = 9;
			date_value = false;
			break;
		}
	}

	@Override
	public BigInteger valInt() {
		MySQLTime ltime = new MySQLTime();
		int week_format;
		long neg;
		if (date_value) {
			if (getArg0Date(ltime, MyTime.TIME_FUZZY_DATE))
				return BigInteger.ZERO;
			neg = 1;
		} else {
			if (getArg0Time(ltime))
				return BigInteger.ZERO;
			;
			neg = ltime.neg ? -1 : 1;
		}
		switch (int_type) {
		case YEAR:
			return BigInteger.valueOf(ltime.year);
		case YEAR_MONTH:
			return BigInteger.valueOf(ltime.year * 100L + ltime.month);
		case QUARTER:
			return BigInteger.valueOf((ltime.month + 2) / 3);
		case MONTH:
			return BigInteger.valueOf(ltime.month);
		case WEEK: {
			week_format = MyTime.WEEK_MONDAY_FIRST;
			long ret = MyTime.calc_week(ltime, MyTime.week_mode(week_format), new LongPtr(0));
			return BigInteger.valueOf(ret);

		}
		case DAY:
			return BigInteger.valueOf(ltime.day);
		case DAY_HOUR:
			return BigInteger.valueOf((ltime.day * 100L + ltime.hour) * neg);
		case DAY_MINUTE:
			return (BigInteger.valueOf((ltime.day * 10000L + ltime.hour * 100L + ltime.minute) * neg));
		case DAY_SECOND:
			return BigInteger
					.valueOf((ltime.day * 1000000L + (ltime.hour * 10000L + ltime.minute * 100 + ltime.second)) * neg);
		case HOUR:
			return BigInteger.valueOf(ltime.hour * neg);
		case HOUR_MINUTE:
			return BigInteger.valueOf((ltime.hour * 100 + ltime.minute) * neg);
		case HOUR_SECOND:
			return BigInteger.valueOf((ltime.hour * 10000 + ltime.minute * 100 + ltime.second) * neg);
		case MINUTE:
			return BigInteger.valueOf(ltime.minute * neg);
		case MINUTE_SECOND:
			return BigInteger.valueOf((ltime.minute * 100 + ltime.second) * neg);
		case SECOND:
			return BigInteger.valueOf(ltime.second * neg);
		case MICROSECOND:
			return BigInteger.valueOf(ltime.second_part * neg);
		case DAY_MICROSECOND:
			return BigInteger.valueOf(
					((ltime.day * 1000000L + ltime.hour * 10000L + ltime.minute * 100 + ltime.second) * 1000000L
							+ ltime.second_part) * neg);
		case HOUR_MICROSECOND:
			return BigInteger.valueOf(
					((ltime.hour * 10000L + ltime.minute * 100 + ltime.second) * 1000000L + ltime.second_part) * neg);
		case MINUTE_MICROSECOND:
			return BigInteger.valueOf((((ltime.minute * 100 + ltime.second)) * 1000000L + ltime.second_part) * neg);
		case SECOND_MICROSECOND:
			return BigInteger.valueOf((ltime.second * 1000000L + ltime.second_part) * neg);
		}
		return BigInteger.ZERO; // Impossible
	}

	@Override
	public SQLExpr toExpression() {
		MySqlExtractExpr extract = new MySqlExtractExpr();
		extract.setValue(args.get(0).toExpression());
		extract.setUnit(int_type);
		return extract;
	}

	@Override
	protected Item cloneStruct(boolean forCalculate, List<Item> calArgs, boolean isPushDown, List<Field> fields) {
		List<Item> newArgs = null;
		if (!forCalculate)
			newArgs = cloneStructList(args);
		else
			newArgs = calArgs;
		return new ItemExtract(newArgs.get(0), int_type);
	}
}
