package com.pi.gl.graphics.objects.lsystem;

import java.util.Arrays;

public class TupleConfig {
	private final char[] variables;
	private final String[] rules;
	private final float[] evolutionaryChance;

	public TupleConfig(final char[] vars, final String[] rules,
			final float[] evolutionaryChance) {
		char[] vC = vars.clone();
		Arrays.sort(vC);
		if (!Arrays.equals(vC, vars)) {
			throw new RuntimeException("Vars it not sorted!");
		}
		if (rules.length != vars.length)
			throw new RuntimeException("Rules length mismatch");
		this.variables = vars;
		for (int i = 0; i < rules.length; i++) {
			rules[i] = rules[i].toLowerCase();
		}
		this.rules = rules;
		if (evolutionaryChance != null) {
			if (evolutionaryChance.length != vars.length)
				throw new RuntimeException(
						"Evolutionary Chance Mismatch");
			this.evolutionaryChance = evolutionaryChance;
		} else {
			this.evolutionaryChance = new float[rules.length];
			Arrays.fill(this.evolutionaryChance, 1f);
		}
	}

	public char[] getVariables() {
		return variables;
	}

	public String[] getRules() {
		return rules;
	}

	public String apply(String inS) {
		StringBuilder b = new StringBuilder();
		proc(b, inS);
		return b.toString();
	}

	private void proc(StringBuilder b, String inS) {
		char[] in = inS.toLowerCase().toCharArray();
		int vI;
		int braces = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == '{')
				braces++;
			else if (in[i] == '}')
				braces--;
			else if (in[i] == '(') {
				int nP = inS.indexOf(')', i);
				if (nP == -1) {
					throw new RuntimeException(
							"Unclosed parentheses!");
				}
				String sub = inS.substring(i + 1, nP);
				String[] data = sub.split("\\?", 2);
				if (data.length == 1) {
					throw new RuntimeException("No '?'!");
				}
				i = nP;
				String[] args = data[1].split(":");
				if (Math.random() <= Double.valueOf(data[0])) {
					proc(b, args[0]);
				} else if (args.length > 1) {
					proc(b, args[0]);
				}
				continue;
			} else if (braces <= 0
					&& (vI =
							Arrays.binarySearch(variables, in[i])) >= 0) {
				if (Math.random() < evolutionaryChance[vI]) {
					b.append(rules[vI]);
					continue;
				}
			}
			b.append(in[i]);
		}
	}
}
