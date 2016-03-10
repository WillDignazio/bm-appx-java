/**
 * Copyright (C) 2016 William Ziener-Dignazio
 * Approximage Boyer-Moore Search Utility
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class BMAppx {
    static class Tuple<T1, T2> {
	T1 left;
	T2 right;
	
	public Tuple(T1 _left, T2 _right) {
	    this.left = _left;
	    this.right = _right;
	}

	@Override
	public String toString() {
	    return "(" + left + "," + right + ")";
	}
    }
    
    static class Table<T1, T2, R> {
	R _default;
	Map<T1, Map<T2, R>> map = new HashMap<>();

	public Table() {
	    this._default = null;
	}

	public Table(R _def) {
	    this._default = _def;
	}

	public R get(T1 t1, T2 t2) {
	    Map<T2, R> inner = map.get(t1);
	    if (inner == null) {
		return _default;
	    }

	    return inner.get(t2);
	}

	public void put(T1 t1, T2 t2, R val) {
	    Map<T2, R> inner;
	    
	    if ((inner = map.get(t1)) == null) {
		inner = new HashMap<T2, R>();
		map.put(t1, inner);
	    }

	    inner.put(t2, val);
	}
    }

    public static char[] getAlphabet(String text) {
	HashSet<Character> out = new HashSet<>();
	for (int idx=0; idx < text.length(); ++idx) {
	    out.add(text.charAt(idx));
	}

	char[] alphabet = new char[out.size()];
	int idx=0;
	for (Character ch : out) {
	    alphabet[idx] = ch;
	    ++idx;
	}
	return alphabet;
    }
    
    public static List<Tuple<Integer, String>> appx_search(String text, String pattern, int k) {
	if (k > pattern.length()) {
	    System.err.println("ERROR: K > pattern.length");
	    return new ArrayList<>();
	}

	char[] alphabet = getAlphabet(text);
	System.out.print("Alphabet: ");
	for (int idx=0; idx < alphabet.length; ++idx) {
	    System.out.print("" + alphabet[idx] + " ");
	} System.out.println();
	
	int n = text.length();
	int m = pattern.length();

	System.out.println("# Approximate Boyer-Moore Search:\n" +
			   "\ttext: " + (text.length() > 10 ? text.substring(0, 10) + "..." : text) + "\n" +
			   "\tpattern: " + (pattern.length() > 10 ? text.substring(0, 10) + "..." : pattern) + "\n" +
			   "\tn = " + n + "\n" +
			   "\tm = " + m);
	
	HashMap<Character, Integer> ready = new HashMap<>();
	Table<Integer, Character, Integer> dk = new Table<>(m+1);

	System.out.println();
	System.out.println("## Preprocessing....");
	
	for (int chidx=0; chidx < alphabet.length; ++chidx) {
	    System.out.println("Installing '" + alphabet[chidx] + "' as " + m + " into ready[]");
	    ready.put(alphabet[chidx], m);
	}

	for (int chidx=0; chidx < alphabet.length; ++chidx) {
	    for (int idx=m; idx > m-k; --idx) {
		System.out.println("Installing " + m + " into Dk[" + idx + "," + alphabet[chidx] + "]");
		dk.put(idx, alphabet[chidx], m);
	    }
	}

	for (int idx=m-1; idx > 1; --idx) {
	    for (int jdx=ready.get(pattern.charAt(idx)); jdx > Math.max(idx, m-k); --jdx) {
		System.out.println("Setting Dk[" + jdx + "," + pattern.charAt(idx) + "] as " + (jdx-idx));
		dk.put(jdx, pattern.charAt(idx), jdx-idx);
	    }
	    System.out.println("Setting ready[" + pattern.charAt(idx) + "] to " + (m-k));
	    ready.put(pattern.charAt(idx), m-k);
	}

	/* Added for list of results */
	List<Tuple<Integer, String>> results = new ArrayList<>();

	System.out.println();
	System.out.println("## Searching...");
	
	int j = m;
	while (j < text.length()) {
	    int h = j;
	    int i = m;
	    int neq = 0;

	    int d = m - k;
	    while (i > 0 && neq <= k) {
		if (i >= m-k) {
		    d = Math.min(d, dk.get(i, text.charAt(h)));
		}

		if (text.charAt(h-1) != pattern.charAt(i-1)) {
		    neq = neq + 1;
		}
		
		i = i - 1;		    
		h = h - 1;
	    }

	    if (neq <= k) {
		 // Added - m for relative to text
		results.add(new Tuple<Integer, String>(j - m, text.substring(j-m, j)));
	    }
	    j = j + (d == 0 ? 1 : d);
	}
	
	return results;
    }
    
    public static void main(String[] args) {
	if (args.length != 3) {
	    System.err.println("Usage: java BMAppx <pattern> <text> <k>");
	    System.exit(1);
	}

	
	int k = Integer.parseInt(args[2]);
	String text = args[1];
	String pattern = args[0];
	
	System.out.println("Matches: " + appx_search(text, pattern, k));
    }
}
