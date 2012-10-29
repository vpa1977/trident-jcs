package moa.trident.state.jcs;

import storm.trident.operation.ReducerAggregator;
import storm.trident.tuple.TridentTuple;

public class AggregateMe implements ReducerAggregator<String> {

	public String init() {
		// TODO Auto-generated method stub
		return new String("");
	}

	public String reduce(String curr, TridentTuple tuple) {
		Object value = tuple.getValue(0);
		return String.valueOf(value);
	}

}
