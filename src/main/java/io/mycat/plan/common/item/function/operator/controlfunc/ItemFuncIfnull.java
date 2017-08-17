package io.mycat.plan.common.item.function.operator.controlfunc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import io.mycat.plan.common.MySQLcom;
import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFunc;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncCoalesce;
import io.mycat.plan.common.time.MySQLTime;


public class ItemFuncIfnull extends ItemFuncCoalesce {
	public ItemFuncIfnull(List<Item> args) {
		super(args);
	}

	@Override
	public final String funcName() {
		return "ifnull";
	}

	@Override
	public void fixLengthAndDec() {
		hybrid_type = MySQLcom.agg_result_type(args, 0, 2);
		cached_field_type = MySQLcom.agg_field_type(args, 0, 2);
		maybeNull = args.get(1).maybeNull;
		decimals = Math.max(args.get(0).decimals, args.get(1).decimals);
	}

	@Override
	public int decimalPrecision() {
		int arg0_int_part = args.get(0).decimalIntPart();
		int arg1_int_part = args.get(1).decimalIntPart();
		int max_int_part = Math.max(arg0_int_part, arg1_int_part);
		int precision = max_int_part + decimals;
		return Math.min(precision, MySQLcom.DECIMAL_MAX_PRECISION);
	}

	@Override
	public BigDecimal realOp() {
		BigDecimal value = args.get(0).valReal();
		if (!args.get(0).nullValue) {
			nullValue = false;
			return value;
		}
		value = args.get(1).valReal();
		if ((nullValue = args.get(1).nullValue))
			return BigDecimal.ZERO;
		return value;
	}

	@Override
	public BigInteger intOp() {
		BigInteger value = args.get(0).valInt();
		if (!args.get(0).nullValue) {
			nullValue = false;
			return value;
		}
		value = args.get(1).valInt();
		if ((nullValue = args.get(1).nullValue))
			return BigInteger.ZERO;
		return value;
	}

	@Override
	public String strOp() {
		String value = args.get(0).valStr();
		if (!args.get(0).nullValue) {
			nullValue = false;
			return value;
		}
		value = args.get(1).valStr();
		if ((nullValue = args.get(1).nullValue))
			return null;
		return value;
	}

	@Override
	public BigDecimal decimalOp() {
		BigDecimal value = args.get(0).valDecimal();
		if (!args.get(0).nullValue) {
			nullValue = false;
			return value;
		}
		value = args.get(1).valDecimal();
		if ((nullValue = args.get(1).nullValue))
			return null;
		return value;
	}

	@Override
	public boolean dateOp(MySQLTime ltime, long fuzzydate) {
		if (!args.get(0).getDate(ltime, fuzzydate))
			return (nullValue = false);
		return (nullValue = args.get(1).getDate(ltime, fuzzydate));
	}

	@Override
	public boolean timeOp(MySQLTime ltime) {
		if (!args.get(0).getTime(ltime))
			return (nullValue = false);
		return (nullValue = args.get(1).getTime(ltime));
	}
	
	@Override
	public ItemFunc nativeConstruct(List<Item> realArgs) {
		return new ItemFuncIfnull(realArgs);
	}
}
