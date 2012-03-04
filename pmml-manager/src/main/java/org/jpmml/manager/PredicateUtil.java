/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.manager;

import java.math.*;
import java.util.*;

import org.dmg.pmml.*;

public class PredicateUtil {

	private PredicateUtil(){
	}

	static
	public Boolean evaluatePredicate(Predicate predicate, Map<FieldName, ?> parameters){

		if(predicate instanceof SimplePredicate){
			return evaluateSimplePredicate((SimplePredicate)predicate, parameters);
		} else

		if(predicate instanceof CompoundPredicate){
			return evaluateCompoundPredicate((CompoundPredicate)predicate, parameters);
		} else

		if(predicate instanceof SimpleSetPredicate){
			return evaluateSimpleSetPredicate((SimpleSetPredicate)predicate, parameters);
		} else

		if(predicate instanceof True){
			return evaluateTruePredicate((True)predicate);
		} else

		if(predicate instanceof False){
			return evaluateFalsePredicate((False)predicate);
		}

		throw new EvaluationException();
	}

	static
	public Boolean evaluateSimplePredicate(SimplePredicate simplePredicate, Map<FieldName, ?> parameters){
		Object fieldValue = ModelManager.getParameterValue(parameters, simplePredicate.getField(), true);

		switch(simplePredicate.getOperator()){
			case IS_MISSING:
				return Boolean.valueOf(fieldValue == null);
			case IS_NOT_MISSING:
				return Boolean.valueOf(fieldValue != null);
			default:
				break;
		}

		if(fieldValue == null){
			return null;
		}

		String value = simplePredicate.getValue();

		switch(simplePredicate.getOperator()){
			case EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) == 0);
			case NOT_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) != 0);
			case LESS_THAN:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) < 0);
			case LESS_OR_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) <= 0);
			case GREATER_THAN:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) > 0);
			case GREATER_OR_EQUAL:
				return Boolean.valueOf(PredicateUtil.compare(fieldValue, value) >= 0);
			default:
				break;
		}

		throw new EvaluationException();
	}

	static
	public Boolean evaluateCompoundPredicate(CompoundPredicate compoundPredicate, Map<FieldName, ?> parameters){
		List<Predicate> predicates = compoundPredicate.getContent();

		Boolean result = evaluatePredicate(predicates.get(0), parameters);

		switch(compoundPredicate.getBooleanOperator()){
			case AND:
			case OR:
			case XOR:
				break;
			case SURROGATE:
				if(result != null){
					return result;
				}
				break;
		}

		for(Predicate predicate : predicates.subList(1, predicates.size())){
			Boolean value = evaluatePredicate(predicate, parameters);

			switch(compoundPredicate.getBooleanOperator()){
				case AND:
					result = PredicateUtil.binaryAnd(result, value);
					break;
				case OR:
					result = PredicateUtil.binaryOr(result, value);
					break;
				case XOR:
					result = PredicateUtil.binaryXor(result, value);
					break;
				case SURROGATE:
					if(value != null){
						return value;
					}
					break;
			}
		}

		return result;
	}

	static
	public Boolean evaluateSimpleSetPredicate(SimpleSetPredicate simpleSetPredicate, Map<FieldName, ?> parameters){
		throw new EvaluationException();
	}

	static
	public Boolean evaluateTruePredicate(True truePredicate){
		return Boolean.TRUE;
	}

	static
	public Boolean evaluateFalsePredicate(False falsePredicate){
		return Boolean.FALSE;
	}

	static
    public int compare(Object left, String right){

    	if(left instanceof Number){
    		return (new BigDecimal(String.valueOf(left))).compareTo(new BigDecimal(right));
    	} else

    	{
    		return (String.valueOf(left)).compareTo(right);
    	}
    }

	static
    public Boolean binaryAnd(Boolean left, Boolean right){

    	if(left == null){

    		if(right == null || right.booleanValue()){
    			return null;
    		} else {
    			return Boolean.FALSE;
    		}
    	} else

    	if(right == null){

    		if(left == null || left.booleanValue()){
    			return null;
    		} else {
    			return Boolean.FALSE;
    		}
    	} else

    	{
    		return Boolean.valueOf(left.booleanValue() & right.booleanValue());
    	}
    }

	static
    public Boolean binaryOr(Boolean left, Boolean right){

    	if(left != null && left.booleanValue()){
    		return Boolean.TRUE;
    	} else

    	if(right != null && right.booleanValue()){
    		return Boolean.TRUE;
    	} else

    	if(left == null || right == null){
    		return null;
    	} else

    	{
    		return Boolean.valueOf(left.booleanValue() | right.booleanValue());
    	}
    }

	static
    public Boolean binaryXor(Boolean left, Boolean right){

    	if(left == null || right == null){
    		return null;
    	} else

    	{
    		return Boolean.valueOf(left.booleanValue() ^ right.booleanValue());
    	}
    }
}