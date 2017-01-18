import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;

class homework {
	BufferedReader br;
	PrintWriter out;
	StringTokenizer st;
	boolean eof;
	private static ArrayList<QueryElement> queries = null;
	private static ArrayList<String> sentences = null;
	private static ArrayList<String> sent = new ArrayList<String>();
	ArrayList<ArrayList<QueryElement>> hm = null;
	ArrayList<ArrayList<QueryElement>> hmp = null;
	PrintWriter fw;

	homework() throws IOException, FileNotFoundException {
		int filecount = 17;
		String file = "input.txt";
		br = new BufferedReader(new FileReader(file));
		fw = new PrintWriter("output.txt", "UTF-8");
		out = new PrintWriter(System.out);
		queries = new ArrayList<QueryElement>();
		sentences = new ArrayList<String>();
		hm = new ArrayList<ArrayList<QueryElement>>();
		getInput();
		processSentences();
		hmp = new ArrayList<ArrayList<QueryElement>>(hm);
		// printMap();
		processQueries();
		out.close();
		br.close();
		fw.close();
	}

	private void processQueries() {
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<QueryElement> queryClause = new ArrayList<QueryElement>();
			QueryElement x = queries.get(i);
			queries.set(i, negateTheElement(x));
			x = queries.get(i);
			queryClause.add(x);

			boolean answer = kbResolve(hm, queryClause, 1);

			if (answer == true) {
				//System.out.println("TRUE");
				if (i == queries.size() - 1) {
					fw.print("TRUE");
				} else {
					fw.println("TRUE");
				}

			} else {
				//System.out.println("FALSE");
				if (i == queries.size() - 1) {
					fw.print("FALSE");
				} else {
					fw.println("FALSE");
				}

			}

		}
	}

	private HashMap<String, String> unifyLiterals(String literal1, String literal2,
			HashMap<String, String> replacements) {

		if (replacements == null) {

			return null;
		} else if (literal1.equals(literal2)) {

			return replacements;
		} else if (typeOfElement(literal1) == 1 && typeOfElement(literal2) == 0) {

			return unifyLiteralVariables(literal1, literal2, replacements);
		} else if (typeOfElement(literal2) == 1 && typeOfElement(literal1) == 0) {

			return unifyLiteralVariables(literal2, literal1, replacements);
		} else if (typeOfElement(literal1) == 2 && typeOfElement(literal2) == 2) {

			QueryElement AX = (QueryElement) convertQuery(literal1);
			QueryElement AY = (QueryElement) convertQuery(literal2);
			return unifyLiterals((AX.getArgumentsInString()), (AY.getArgumentsInString()),
					unifyLiterals(AX.getPredicate(), AY.getPredicate(), replacements));
		} else if (typeOfElement(literal1) == 3 && typeOfElement(literal2) == 3) {

			return unifyLiterals(getRemainingArguments(literal1), getRemainingArguments(literal2),
					unifyLiterals(getFirstArgument(literal1), getFirstArgument(literal2), replacements));
		} else {
			return null;
		}
	}

	public static String getFirstArgument(String list) {
		String[] commaTokens = list.split(",");
		return commaTokens[0];
	}

	public static String getRemainingArguments(String list) {
		String[] commaTokens = list.split(",");
		StringBuilder str = new StringBuilder();
		for (int i = 1; i < commaTokens.length - 1; i++) {
			str.append(commaTokens[i] + ",");
		}
		str.append(commaTokens[commaTokens.length - 1]);

		return str.toString();
	}

	private HashMap<String, String> unifyLiteralVariables(String variable, String x,
			HashMap<String, String> replacements) {
		if (replacements.containsKey(variable)) {

			return unifyLiterals(replacements.get(variable), x, replacements);
		} else if (replacements.containsKey(x)) {

			return unifyLiterals(variable, replacements.get(x), replacements);
		} else if (sVariables(variable, x, replacements)) {

			return null;
		}
		replacements.put(variable, x);

		return replacements;
	}

	private boolean sVariables(String variable, String x, HashMap<String, String> replacements) {
		if (variable.equals(x)) {

			return true;
		} else if (replacements.containsKey(x)) {

			return sVariables(variable, replacements.get(x), replacements);
		} else if (typeOfElement(x) == 2) {
			QueryElement AY = (QueryElement) convertQuerySentence(x);
			for (int z = 0; z < AY.getArguments().size(); z++) {
				if (sVariables(variable, AY.getArg(z), replacements)) {

					return true;
				}
			}
		}

		return false;
	}

	private int typeOfElement(String literal) {
		if (literal.split("\\(").length > 1) {

			return 2;
		}
		if (literal.split(",").length > 1) {

			return 3;
		}
		if (Character.isUpperCase(literal.charAt(0))) {

			return 0;
		}
		if (Character.isLowerCase(literal.charAt(0))) {

			return 1;
		}

		return 99;
	}

	public boolean isComplimentory(QueryElement a, QueryElement b) {
		if ((a.getPredicate().charAt(0) == '~' && b.getPredicate().charAt(0) != '~')
				|| (a.getPredicate().charAt(0) != '~' && b.getPredicate().charAt(0) == '~')) {

			return true;
		} else {

			return false;
		}
	}

	public QueryElement exc(QueryElement literal, HashMap<String, String> replacements) {

		ArrayList<String> a = new ArrayList<String>();
		for (int j = 0; j < literal.getArguments().size(); j++) {
			a.add(literal.getArg(j));
		}
		QueryElement q = new QueryElement(literal.getPredicate(), a);
		for (int j = 0; j < literal.getArguments().size(); j++) {
			if (replacements.keySet().contains(literal.getArg(j))) {
				q.setArg(j, replacements.get(literal.getArg(j)));
			}
		}

		return q;
	}

	/*
	 * public void printclause(ArrayList<QueryElement> c1) { for (int i = 0; i <
	 * c1.size(); i++) { } }
	 */

	public boolean kbResolve(ArrayList<ArrayList<QueryElement>> kb, ArrayList<QueryElement> queryClause, int d) {

		ArrayList<QueryElement> c1;
		ArrayList<QueryElement> c2;
		c1 = queryClause;
		if (d > 1000)

			return false;
		for (int i = 0; i < c1.size(); i++) {
			QueryElement L1;
			L1 = queryClause.get(i);
			for (int q = 0; q < kb.size(); q++) {
				c2 = kb.get(q);
				// printclause(c2);
				QueryElement L2;

				for (int j = 0; j < c2.size(); j++) {
					L2 = c2.get(j);
					/*if (L1.getStringForm().equals(L2.getStringForm())
							&& !L1.getnegStringForm().equals(L2.getnegStringForm()))

						return true;*/
					boolean isComp = isComplimentory(L1, L2);
					if (isComp == true) {
						HashMap<String, String> uni = unifyLiterals(L1.getStringForm(), L2.getStringForm(),
								new HashMap<String, String>());
						if (uni != null) {
							ArrayList<QueryElement> c1copy = c1;
							ArrayList<QueryElement> c2copy = c2;
							ArrayList<QueryElement> newC = Resolve(c1copy, c2copy, i, j, uni);
							if (newC.size() == 0) {

								return true;
							} else {
								if (kbResolve(kb, newC, d++) == true) {

									return true;
								} else {
									if (d > 1000) {

										return false;
									}
									/*
									 * if (q == kb.size() - 1) return false;
									 */
								}
							}
						}

					}
				}
			}
		}

		return false;
	}

	public QueryElement negateTheElement(QueryElement a) {
		if (a.getPredicate().charAt(0) == '~') {
			a.setPredicate(a.getPredicate().substring(1, a.getPredicate().length()));
		} else {
			a.setPredicate("~" + a.getPredicate());
		}

		return a;
	}

	private ArrayList<QueryElement> Resolve(ArrayList<QueryElement> c1copy, ArrayList<QueryElement> c2copy, int i,
			int j, HashMap<String, String> uni) {
		ArrayList<QueryElement> newClause = new ArrayList<QueryElement>();

		for (int a = 0; a < c2copy.size(); a++) {
			if (a != j) {
				newClause.add(exc(c2copy.get(a), uni));
			}
		}
		for (int a = 0; a < c1copy.size(); a++) {
			if (a != i) {

				newClause.add(exc(c1copy.get(a), uni));
			}
		}

		return newClause;
	}

	public static void main(String[] args) throws IOException {
		new homework();
	}

	void getInput() throws IOException {
		int noOfQueries = getInt();
		for (int i = 0; i < noOfQueries; i++) {
			queries.add(convertQueryInput(getEntireLine()));
		}
		int noOfSentences = getInt();
		for (int i = 0; i < noOfSentences; i++) {
			sentences.add(getEntireLine());
		}
	}

	private void processSentences() {
		int slen = sentences.size();
		String a = "";
		for (int k = 0; k < slen; k++) {
			String[] arr = null;
			OperateSentence os = new OperateSentence(sentences.get(k));
			os.setSentence(os.modifyBrackets(os.getSentence()));
			os.toPrefix();
			os.toCNF();
			os.toInfix();
			arr = os.getSentenceArray();
			int len = arr.length;
			for (int i = 0; i < len; i++) {
				String sentence = arr[i];
				sent.add(arr[i]);
				ArrayList<QueryElement> sss = new ArrayList<QueryElement>();
				for (int j = 0; j < sentence.length(); j++) {
					char x = sentence.charAt(j);
					if (x == '&' || x == '|') {

					} else {
						a = a + ((Character) x).toString();
						if (x == '}') {
							sss.add((QueryElement) convertQuerySentence(a));
							a = "";
						}
					}
				}
				hm.add(sss);
			}
		}
	}

	public static QueryElement convertQuerySentence(String line) {
		String predicate = line.split("\\{")[0];
		String argumentsStr = line.split("\\{")[1].split("\\}")[0];
		String[] argumentsArr = argumentsStr.split(",");
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(argumentsArr));

		return new QueryElement(predicate, arguments);
	}

	public static QueryElement convertQueryInput(String line) {
		line = line.replaceAll(" ", "");
		if (line.charAt(0) == '~' && line.charAt(1) == '(') {
			line = "~" + line.substring(2, line.length() - 1);
		}
		String predicate = line.split("\\(")[0];
		String argumentsStr = line.split("\\(")[1].split("\\)")[0];
		String[] argumentsArr = argumentsStr.split(",");
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(argumentsArr));

		return new QueryElement(predicate, arguments);
	}

	public static QueryElement convertQuery(String line) {
		String predicate = line.split("\\(")[0];
		String argumentsStr = line.split("\\(")[1].split("\\)")[0];
		String[] argumentsArr = argumentsStr.split(",");
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(argumentsArr));

		return new QueryElement(predicate, arguments);
	}

	private void printQueries() {
		out.println("Size " + queries.size());
		for (int i = 0; i < queries.size(); i++) {
			ArrayList<String> a = queries.get(i).getArguments();
			out.print(queries.get(i).getPredicate() + " has " + a);
			out.println();
		}
	}

	private void printSentences() {
		out.println("Size " + sentences.size());
		for (int i = 0; i < sentences.size(); i++) {
			out.print(sentences.get(i));
			out.println();
		}
	}

	private void printMap() {
		for (int i = 0; i < hm.size(); i++) {

			out.println();
			out.println("Sentence # " + i);
			ArrayList<QueryElement> sss = hm.get(i);
			int slen = sss.size();
			for (int k = 0; k < slen; k++) {
				if (sss.get(k) instanceof QueryElement) {
					out.print(((QueryElement) sss.get(k)).getPredicate() + ((QueryElement) sss.get(k)).getArguments());
				} else {
					out.print(sss.get(k).toString());
				}

			}
			out.println();
		}
	}

	private void printSent() {
		out.println("Size " + sent.size());
		for (int i = 0; i < sent.size(); i++) {
			out.print(sent.get(i));
			out.println();
		}
	}

	String nextToken() {
		while (st == null || !st.hasMoreTokens()) {
			try {
				st = new StringTokenizer(br.readLine());
			} catch (Exception e) {
				eof = true;

				return null;
			}
		}

		return st.nextToken();
	}

	String getEntireLine() {
		try {
			return br.readLine();
		} catch (IOException e) {
			eof = true;

			return null;
		}
	}

	String getNextWORD() {

		return nextToken();
	}

	int getInt() throws IOException {

		return Integer.parseInt(nextToken());
	}

	long getLong() throws IOException {

		return Long.parseLong(nextToken());
	}

	double getDouble() throws IOException {

		return Double.parseDouble(nextToken());
	}
}

class QueryElement {

	private String predicate = null;

	private ArrayList<String> arguments = null;

	public QueryElement() {
		predicate = new String();
		arguments = new ArrayList<String>();
	}

	public String getArgumentsInString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < this.arguments.size() - 1; i++) {
			str.append(this.arguments.get(i) + ",");
		}
		str.append(this.arguments.get(this.arguments.size() - 1));

		return str.toString();
	}

	public QueryElement(String p, ArrayList<String> a) {
		this.predicate = p;
		this.arguments = new ArrayList<String>(a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {

			return true;
		}
		if (!(obj instanceof QueryElement)) {

			return false;
		}
		QueryElement that = (QueryElement) obj;
		if (!this.predicate.equals(that.getPredicate())) {

			return false;
		}
		for (int i = 0; i < this.arguments.size(); i++) {
			if (!this.arguments.get(i).equals(that.getArg(i))) {

				return false;
			}
		}

		return true;
	}

	public String getPredicate() {

		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public ArrayList<String> getArguments() {

		return arguments;
	}

	public void setArg(int idx, String value) {
		arguments.set(idx, value);
	}

	public String getArg(int idx) {

		return arguments.get(idx);
	}

	public Boolean isNot() {
		if (this.predicate.charAt(0) == '~') {

			return true;
		}

		return false;
	}

	public String getStringForm() {
		String res = String.join(",", this.arguments);
		String removed = this.predicate;
		if (this.predicate.charAt(0) == '~') {
			removed = this.predicate.substring(1, this.predicate.length());
		}
		String str = removed + "(" + res + ")";

		return str;
	}

	public String getnegStringForm() {
		String res = String.join(",", this.arguments);
		String removed = this.predicate;
		String str = removed + "(" + res + ")";

		return str;
	}

}

class OperateSentence {
	String sentence = null;
	String implies = "=";
	String or = "|";
	String and = "&";
	String not = "~";
	String openCurly = "{";
	String closeCurly = "}";
	String openSquare = "[";
	String closeSquare = "]";
	String[] splitSentences;

	public OperateSentence(String str) {
		this.sentence = str;
	}

	public String[] getSentenceArray() {

		return this.splitSentences;
	}

	private static String replaceElementAtIndex(String str, String element, int index) {
		str = str.substring(0, index) + element + str.substring(index + 1);

		return str;
	}

	boolean needToOperate() {
		if (sentence.contains(and) || sentence.contains(implies) || sentence.contains(or)) {

			return true;
		}

		return false;
	}

	String modifyBrackets(String str) {
		str = str.replaceAll(" ", "");
		str = str.replaceAll("=>", implies);
		str = str.replaceAll("&", and);
		str = str.replaceAll("\\|", or);
		int j = 0;
		int k = 0;
		boolean found = false;
		for (int i = 0; i < str.length(); i++) {
			char x = str.charAt(i);
			if (x == '(') {
				found = true;
				j = i;
			} else if (x == ')') {
				found = true;
				k = i;
				break;
			}
		}
		if (found) {
			if ((str.substring(j + 1, k).contains(and)) || (str.substring(j + 1, k).contains(implies))
					|| (str.substring(j + 1, k).contains(or)) || (str.substring(j + 1, k).contains(not))) {
				str = replaceElementAtIndex(str, openSquare, j);
				str = replaceElementAtIndex(str, closeSquare, k);
			} else {
				str = replaceElementAtIndex(str, openCurly, j);
				str = replaceElementAtIndex(str, closeCurly, k);
			}

			return modifyBrackets(str);
		} else {

			return str;
		}
	}

	public void toPrefix() {
		infixToPrefix ip = new infixToPrefix(sentence);
		this.sentence = ip.convert();
	}

	public void toCNF() {
		prefixToCNF pc = new prefixToCNF(sentence);
		this.sentence = pc.convert();
	}

	public void toInfix() {
		CNFtoInfix ci = new CNFtoInfix(sentence);
		splitSentences = ci.convert();
	}

	public String getSentence() {

		return this.sentence;
	}

	public void setSentence(String str) {
		this.sentence = str;
	}
}

class infixToPrefix {
	String sentence = null;
	ArrayList<String> elementlist = null;

	public infixToPrefix(String str) {
		this.sentence = str;
		this.elementlist = new ArrayList<String>();
	}

	public String getSentence() {

		return this.sentence;
	}

	public String convert() {
		String a = "";
		for (int i = 0; i < sentence.length(); i++) {
			if (sentence.charAt(i) != '[' && sentence.charAt(i) != ']' && sentence.charAt(i) != '&'
					&& sentence.charAt(i) != '~' && sentence.charAt(i) != '|' && sentence.charAt(i) != '=') {
				a = a + sentence.charAt(i);
				if (sentence.charAt(i) == '{') {
					while (sentence.charAt(i) != '}') {
						if (i != sentence.length() - 1) {
							a = a + sentence.charAt(i + 1);
							i = i + 1;
						}
					}
					if (i == sentence.length() - 1) {
						elementlist.add(a);
					}
				}
			} else {
				if (!a.equals("")) {
					elementlist.add(a);
					a = "";
				}
				String z = sentence.charAt(i) + "";
				elementlist.add(z);
			}
		}
		String[] tokens = new String[elementlist.size()];
		tokens = elementlist.toArray(tokens);
		Stack<String> values = new Stack<String>();
		Stack<String> ops = new Stack<String>();

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(" "))
				continue;
			if (!tokens[i].equals("&") && !tokens[i].equals("|") && !tokens[i].equals("=") && !tokens[i].equals("[")
					&& !tokens[i].equals("]") && !tokens[i].equals("~")) {
				StringBuffer sbuf = new StringBuffer();
				sbuf.append(tokens[i]);
				values.push(sbuf.toString());
			} else if (tokens[i].equals("[")) {
				ops.push(tokens[i]);
			} else if (tokens[i].equals("]")) {
				while (!ops.peek().equals("["))
					if (ops.peek().equals("~")) {
						values.push(affect(ops.pop(), values.pop(), "abc"));
					} else {
						values.push(affect(ops.pop(), values.pop(), values.pop()));
					}
				ops.pop();
			} else if (tokens[i].equals("&") || tokens[i].equals("|") || tokens[i].equals("=")
					|| tokens[i].equals("~")) {
				while (!ops.empty() && logicprecedence(tokens[i], ops.peek())) {
					if (ops.peek().equals("~")) {
						values.push(affect(ops.pop(), values.pop(), "abc"));
					} else {
						values.push(affect(ops.pop(), values.pop(), values.pop()));
					}
				}
				ops.push(tokens[i]);
			} else {

			}
		}
		while (!ops.empty()) {
			if (ops.peek().equals("~")) {
				values.push(affect(ops.pop(), values.pop(), "abc"));
			} else {
				values.push(affect(ops.pop(), values.pop(), values.pop()));
			}
		}
		this.sentence = values.pop();

		return this.sentence;
	}

	private static boolean logicprecedence(String op1, String op2) {
		if (op2.equals("[") || op2.equals("]"))

			return false;
		if ((op1.equals("~") && (op2.equals("&") || op2.equals("|") || op2.equals("=")))
				|| (op1.equals("&") && (op2.equals("|") || op2.equals("="))) || (op1.equals("|") && (op2.equals("="))))

			return false;
		else

			return true;
	}

	private static String affect(String string, String b, String a) {
		String a1;
		String b1;
		if (b.charAt(0) == '[') {
			b1 = b;
		} else {
			b1 = "'" + b + "'";
		}
		if (a.charAt(0) == '[') {
			a1 = a;
		} else {
			a1 = "'" + a + "'";
		}
		switch (string) {
		case "&":

			return "['&'," + a1 + "," + b1 + "]";
		case "|":

			return "['|'," + a1 + "," + b1 + "]";
		case "=":

			return "['='," + a1 + "," + b1 + "]";
		case "~":

			return "['~'," + b1 + "]";
		}

		return null;
	}
}

class prefixToCNF {
	String a = "&";
	String b = "|";
	String c = "~";
	String d = "=";
	String sentence = null;
	List<Object> arrList = null;
	List<Object> charArr = new ArrayList<Object>();

	public prefixToCNF(String str) {
		this.sentence = str;
	}

	public String getSentence() {

		return this.sentence;
	}

	public String convert() {
		arrList = this.parser2(this.sentence);
		arrList = this.Ordimply(arrList);
		arrList = this.checkn(arrList);
		arrList = this.handleDist(arrList);

		String z = arrList.toString().replace("[", "").replace("]", "");
		boolean flag = false;
		for (int i = 0; i < z.length(); i++) {
			if (z.charAt(i) == '{') {
				flag = true;
			}
			if (!flag && z.charAt(i) == ',') {
				z = z.substring(0, i) + z.substring(i + 1, z.length());
			} else if (z.charAt(i) == '}') {
				flag = false;
			}
		}
		this.sentence = z;

		return z;
	}

	private List<Object> parser2(String s) {

		Stack<ArrayList<Object>> stack = new Stack<ArrayList<Object>>();
		char a[] = s.toCharArray();
		if (a[0] != '[') {
			ArrayList<Object> newObj = new ArrayList<Object>();
			newObj.add(s);

			return newObj;
		}
		int u = 0;
		boolean open = false;
		int startIndex = 0;
		while (u < a.length) {
			if (a[u] == '[') {
				stack.push(new ArrayList<Object>());
			} else if (a[u] == '\'') {
				open = !open;
				if (open == true) {
					startIndex = u;
				} else {
					ArrayList<Object> newObj = stack.pop();
					newObj.add(s.substring(startIndex + 1, u));
					stack.push(newObj);
				}
			} else if (a[u] == ']') {
				if (u != a.length - 1) {
					ArrayList<Object> newObj = stack.pop();
					ArrayList<Object> secondlast = stack.pop();
					secondlast.add(newObj);
					stack.push(secondlast);
				}
			}
			u++;
		}

		return stack.pop();
	}

	@SuppressWarnings("unchecked")
	private List<Object> Ordimply(List<Object> pimplied) {
		List<Object> tem = new ArrayList<Object>();
		int len = pimplied.size();
		if (check(pimplied)) {
			pimplied = removeimp(pimplied);
		}
		for (int i = 1; i < len; i++) {
			if (!InstanceCalculator(pimplied.get(i))) {
				tem = (List<Object>) pimplied.get(i);
				int n = tem.size();
				if (n > 1) {
					pimplied.set(i, Ordimply(tem));
				}
			}
		}
		if (check(pimplied)) {
			pimplied = removeimp(pimplied);
		}

		return pimplied;
	}

	private List<Object> removeimp(List<Object> d) {
		List<Object> e = new ArrayList<Object>();
		e.add(b);
		List<Object> tem1 = new ArrayList<Object>();
		tem1.add(c);
		tem1.add(d.get(1));
		e.add(tem1);
		e.add(d.get(2));

		return e;
	}

	private boolean check(List<Object> sense) {
		int len = sense.size();
		boolean flag = true;
		if (InstanceCalculator(sense.get(0)) && len == 3 && sense.get(0).equals(d)) {

			return flag;
		} else {

			return !flag;
		}
	}

	private boolean equation(List<Object> todo) {
		boolean flag = true;
		int len = todo.size();
		if (len == 2 && !InstanceCalculator(todo.get(1)) && InstanceCalculator(todo.get(0)) && todo.get(0).equals(c)) {

			return flag;
		}
		return !flag;
	}

	private boolean InstanceCalculator(Object result) {
		boolean flag = true;
		if (result instanceof String)
			return flag;

		return !flag;
	}

	@SuppressWarnings("unchecked")
	private boolean checkd(List<Object> reason) {
		boolean flag = true;
		List<Object> tem = new ArrayList<Object>();
		if (reason.get(0).equals(b) && InstanceCalculator(reason.get(0))) {
			for (int i = 1; i < reason.size(); i++) {
				if (!InstanceCalculator(reason.get(i))) {
					tem = (List<Object>) reason.get(i);
					if (tem.size() > 1) {
						if (tem.get(0).equals(a) && InstanceCalculator(tem.get(0))) {

							return flag;
						}
					}
				}
			}
		}

		return !flag;
	}

	@SuppressWarnings("unchecked")
	private List<Object> handleDist(List<Object> qr) {
		List<Object> tem = new ArrayList<Object>();
		if (checkd(qr)) {
			qr = checkod(qr);
		}
		for (int i = 1; i < qr.size(); i++) {
			if (!InstanceCalculator(qr.get(i))) {
				tem = (List<Object>) qr.get(i);
				if (tem.size() > 1) {
					qr.set(i, handleDist(tem));
				}
			}
		}
		if (checkd(qr)) {
			qr = checkod(qr);
		}
		return qr;
	}

	@SuppressWarnings("unchecked")
	private List<Object> checkod(List<Object> data) {
		List<Object> end = new ArrayList<Object>(), start = new ArrayList<Object>(), start1 = new ArrayList<Object>();
		end.add(a);
		if (!InstanceCalculator(data.get(1))) {
			start = (List<Object>) data.get(1);
		} else {
			start.add(data.get(1));
		}
		if (!InstanceCalculator(data.get(2))) {
			start1 = (List<Object>) data.get(2);
		} else {
			start1.add(data.get(2));
		}
		if (start.get(0).equals(a) && start1.get(0).equals(a)) {
			List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>(),
					terminate = new ArrayList<Object>(), end2 = new ArrayList<Object>();
			finish = AddElements(b, start.get(1), start1.get(1));
			end.add(handleDist(finish));
			stop = AddElements(b, start.get(1), start1.get(2));
			end.add(handleDist(stop));
			terminate = AddElements(b, start.get(2), start1.get(1));
			end.add(handleDist(terminate));
			end2 = AddElements(b, start.get(2), start1.get(2));
			end.add(handleDist(end2));

		} else {
			if (start.get(0).equals(a)) {
				if (start1.size() > 2) {
					if (checkd(start1)) {
						data.set(2, handleDist(start1));
						List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>(),
								terminate = new ArrayList<Object>(), end2 = new ArrayList<Object>();
						finish = AddElements(b, start.get(1), start1.get(1));
						end.add(handleDist(finish));
						stop = AddElements(b, start.get(1), start1.get(2));
						end.add(handleDist(stop));
						terminate = AddElements(b, start.get(2), start1.get(1));
						end.add(handleDist(terminate));
						end2 = AddElements(b, start.get(2), start1.get(2));
						end.add(handleDist(end2));
					} else {
						List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>();
						finish = AddElements(b, start.get(1), start1);
						end.add(handleDist(finish));
						stop = AddElements(b, start.get(2), start1);
						end.add(handleDist(stop));
					}
				} else {
					List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>();
					finish = AddElements(b, start.get(1), start1);
					end.add(handleDist(finish));
					stop = AddElements(b, start.get(2), start1);
					end.add(handleDist(stop));
				}

			} else {
				if (start.size() > 2) {
					if (checkd(start)) {
						data.set(1, handleDist(start));
						List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>(),
								terminate = new ArrayList<Object>(), end2 = new ArrayList<Object>();
						finish = AddElements(b, start.get(1), start1.get(1));
						end.add(handleDist(finish));
						stop = AddElements(b, start.get(1), start1.get(2));
						end.add(handleDist(stop));
						terminate = AddElements(b, start.get(2), start1.get(1));
						end.add(handleDist(terminate));
						end2 = AddElements(b, start.get(2), start1.get(2));
						end.add(handleDist(end2));
					} else {
						List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>();
						finish = AddElements(b, start, start1.get(1));
						end.add(handleDist(finish));
						stop = AddElements(b, start, start1.get(2));
						end.add(handleDist(stop));
					}
				} else {
					List<Object> finish = new ArrayList<Object>(), stop = new ArrayList<Object>();
					finish = AddElements(b, start, start1.get(1));
					end.add(handleDist(finish));
					stop = AddElements(b, start, start1.get(2));
					end.add(handleDist(stop));
				}
			}
		}

		return end;
	}

	private List<Object> AddElements(String b2, Object object, Object object2) {
		List<Object> ad = new ArrayList<Object>();
		ad.add(b2);
		ad.add(object);
		ad.add(object2);

		return ad;
	}

	@SuppressWarnings("unchecked")
	private List<Object> checkn(List<Object> npar) {
		List<Object> tem = new ArrayList<Object>();
		if (equation(npar)) {
			npar = handlenot(npar);
		}
		int data = npar.size();
		for (int i = 1; i < data; i++) {
			if (!InstanceCalculator(npar.get(i))) {
				tem = (List<Object>) npar.get(i);
				if (tem.size() > 1) {
					npar.set(i, checkn(tem));
				}
			}
		}
		if (equation(npar)) {
			npar = handlenot(npar);
		}

		return npar;
	}

	@SuppressWarnings("unchecked")
	private List<Object> handlenot(List<Object> direct) {
		List<Object> notlist = new ArrayList<Object>(), temp = new ArrayList<Object>(), w = new ArrayList<Object>();
		String temp1 = "";
		if (!InstanceCalculator(direct.get(1))) {
			temp = (List<Object>) direct.get(1);
			temp1 = (String) temp.get(0);
			if (temp1.equals(b))
				notlist.add(a);
			else if (temp1.equals(a))
				notlist.add(b);
			else if (temp1.equals(c)) {
				if (!InstanceCalculator(temp.get(1))) {
					w = (List<Object>) temp.get(1);
				} else {
					w.add(temp.get(1));
				}

				return w;
			}
			for (int i = 1; i < temp.size(); i++) {
				List<Object> tem = new ArrayList<Object>();
				if (!InstanceCalculator(temp.get(i))) {
					tem.add(c);
					tem.add(temp.get(i));
					notlist.add(handlenot(tem));
				} else {
					tem.add(c);
					tem.add(temp.get(i));
					notlist.add(tem);
				}
			}
		}

		return notlist;
	}
}

class CNFtoInfix {
	String sentence = null;
	ArrayList<String> b;
	private static Node up;
	int i = 0;

	public CNFtoInfix(String str) {
		b = new ArrayList<String>();
		up = null;
		this.sentence = str;
	}

	public void clear() {
		up = null;
	}

	public String[] convert() {
		this.treeConstruct(this.sentence);
		this.infix();

		ArrayList<String> d = this.getB();
		String b = "";
		for (int counter = 0; counter < d.size(); counter++) {
			b = b + d.get(counter);
		}
		String arr[] = b.split("&");
		String s = "";
		for (int i = 0; i < arr.length; i++) {
			s = s + arr[i];
		}
		this.sentence = s;

		return arr;
	}

	class Leaf {
		String data;
		Leaf left, right;

		public Leaf(String s) {
			this.data = s;
			this.left = null;
			this.right = null;
		}
	}

	class Node {
		Leaf Tree;
		Node next;

		public Node(Leaf Tree) {
			this.Tree = Tree;
			next = null;
		}
	}

	private void push(Leaf ptr) {
		if (up == null)
			up = new Node(ptr);
		else {
			Node nptr = new Node(ptr);
			nptr.next = up;
			up = nptr;
		}
	}

	public void treeConstruct(String eqn) {
		String[] s = eqn.split(" ");
		for (int i = 0; i < s.length; i++) {
			if (s[i].equals("and")) {
				s[i] = s[i].replaceAll("and", "&");
			}
			if (s[i].equals("or")) {
				s[i] = s[i].replaceAll("or", "|");
			}
			if (s[i].equals("not")) {
				s[i] = s[i].replaceAll("not", "~");
			}
		}
		int len = s.length;
		for (int i = len - 1; i >= 0; i--) {
			if (i != 0) {
				if (s[i - 1].equals("~") && !(s[i].equals("&") || s[i].equals("|"))) {
					insert(s[i - 1] + s[i]);
					i--;
				} else {
					insert(s[i]);
				}
			} else {
				insert(s[i]);
			}
		}
	}

	private void insert(String s) {
		try {
			if (!isOperator(s)) {
				Leaf nptr = new Leaf(s);
				push(nptr);
			} else if (isOperator(s)) {
				Leaf nptr = new Leaf(s);
				nptr.left = pop();
				nptr.right = pop();
				push(nptr);
			}
		} catch (Exception e) {
		}
	}

	private Leaf pop() {
		if (up == null)
			throw new RuntimeException("Empty");
		else {
			Leaf ptr = up.Tree;
			up = up.next;

			return ptr;
		}
	}

	public void infix() {
		inOrder(peek());
	}

	private void inOrder(Leaf ptr) {
		if (ptr != null) {
			inOrder(ptr.left);
			b.add(i, ptr.data);
			i++;
			inOrder(ptr.right);
		}
	}

	private Leaf peek() {

		return up.Tree;
	}

	private boolean isOperator(String ch) {

		return ch.equals("&") || ch.equals("|");
	}

	public ArrayList<String> getB() {
		return b;
	}
}