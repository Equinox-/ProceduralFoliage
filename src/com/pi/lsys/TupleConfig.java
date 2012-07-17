package com.pi.lsys;

import java.util.Arrays;

public class TupleConfig {
    private final char[] variables;
    private final String[] rules;

    public TupleConfig(final char[] vars, final String[] rules) {
	char[] vC = vars.clone();
	Arrays.sort(vC);
	if (!Arrays.equals(vC, vars)) {
	    throw new RuntimeException("Vars it not sorted!");
	}
	this.variables = vars;
	for (int i = 0; i < rules.length; i++) {
	    rules[i] = rules[i].toLowerCase();
	}
	this.rules = rules;
    }

    public char[] getVariables() {
	return variables;
    }

    public String[] getRules() {
	return rules;
    }

    public String apply(String inS) {
	StringBuilder b = new StringBuilder();
	char[] in = inS.toLowerCase().toCharArray();
	int vI;
	for (int i = 0; i < in.length; i++) {
	    if (in[i] == '(') {
		int nP = inS.indexOf(')', i);
		if (nP == -1) {
		    throw new RuntimeException("Unclosed parentheses!");
		}
		String sub = inS.substring(i + 1, nP);
		i = nP;
		if (Math.random() > Double.valueOf(sub)) {
		    i++;
		    continue;
		}
		break;
	    } else if ((vI = Arrays.binarySearch(variables, in[i])) >= 0) {
		b.append(rules[vI]);
	    } else {
		b.append(in[i]);
	    }
	}
	return b.toString();
    }
}
