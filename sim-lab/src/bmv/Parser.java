package bmv;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import bmv.Node.SHAPE;

/**
 * This is a natural language parser designed to parse sentences describing node
 * interactions and generate a model file with these nodes and edges in it. The
 * edges are defaulted to weight 0 and all nodes are set to 2 states, "High" and
 * "Low" by default. Colors and positions of the nodes are also all set by
 * default iterating values
 * 
 * @author plvines
 * 
 */
public class Parser implements Runnable {

	private static Hashtable<Integer, Term> termsDict;
	private static Hashtable<Integer, String> adjectiveDict;
	private static final String DEFAULT_EDGE_NAME = "regulates";

	private final static int NODE_SPACING = 100;
	private static BMVManager pane;
	private static Viewport port;
	private File importFile;
	private Model model;

	/**
	 * Static constructor, use once
	 * PRE: pane and port are defined
	 * POST: if Parser.pane and Parser.port were not already defined, they are
	 * now
	 * 
	 * @param pane
	 * @param port
	 */
	public Parser(BMVManager pane, Viewport port) {

		if (Parser.pane == null) {
			Parser.pane = pane;
		}
		if (Parser.port == null) {
			Parser.port = port;
		}
		initializeDicts();
	}

	/**
	 * Instance constructor, use for each parsing
	 * PRE: referenceModel and file are defined
	 * POST: model and importFile are now set for this Parser object, this is
	 * the model reference the parser will fill when run() is called. If
	 * dictionaries were not already defined, they are now defined
	 * 
	 * @param referenceModel
	 * @param file
	 */
	public Parser(Model referenceModel, File file) {
		importFile = file;
		model = referenceModel;
		initializeDicts();
	}

	/**
	 * PRE:
	 * POST: dictionaries are defined
	 */
	private void initializeDicts() {

		if (Parser.termsDict == null) {
			File termsFile = new File("Resources/termsDictionary.txt");
			termsDict = new Hashtable<Integer, Term>();

			StateVocabulary vocab = pane.vocab;
			for (int i = vocab.getTermSets().size() - 1; i >= 0; i--) {
				ArrayList<Term> termSet = vocab.getTermSets().get(i);
				for (int k = 0; k < termSet.size(); k++) {
					termsDict.put(termSet.get(k).hashCode(), termSet.get(k));
				}
			}
			try {
				Scanner scan = new Scanner(termsFile);
				scan.useDelimiter("(\n|\t)");
				String token;
				while (scan.hasNext()) {
					token = scan.next();
					Term word = new Term(token);
					termsDict.put(word.hashCode(), word);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (Parser.adjectiveDict == null) {
			File actionsFile = new File("Resources/adjectiveDictionary.txt");
			adjectiveDict = new Hashtable<Integer, String>();
			try {
				Scanner scan = new Scanner(actionsFile);
				scan.useDelimiter("(\n|\t)");
				String token;
				while (scan.hasNext()) {
					token = scan.next();
					adjectiveDict.put(token.hashCode(), token);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parses the importFile and places the parsed model in the model class
	 * variable
	 */
	@Override
	public void run() {
		if (importFile != null && port != null && pane != null && model != null) {
			try {
				parse(model, importFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			model.setModelName(importFile.getName().substring(0,
					importFile.getName().indexOf('.')));
			model.saveModel();
			pane.updateBrowser();
		}
	}

	/**
	 * PRE:
	 * POST: parses inputFile into model
	 * 
	 * @param model
	 * @param inputFile
	 * @throws FileNotFoundException
	 */
	public void parse(Model model, File inputFile) throws FileNotFoundException {
		Model newModel = model;

		// scan.useDelimiter(Pattern.compile("(\t|\n)+"));
		String input = "";
		Scanner scan = new Scanner(inputFile);
		scan.useDelimiter("\0");
		for (; scan.hasNext();) {
			input += scan.next();
		}
		Scanner sentenceScanner = new Scanner(input);
		sentenceScanner.useDelimiter(Pattern.compile("\\."));
		ArrayList<ArrayDeque<String>> sentences = new ArrayList<ArrayDeque<String>>();
		while (sentenceScanner.hasNextLine()) {
			ArrayDeque<String> sentence = new ArrayDeque<String>();
			Scanner wordScanner = new Scanner(sentenceScanner.next());
			while (wordScanner.hasNext()) {
				sentence.addLast(wordScanner.next());
			}
			sentences.add(sentence);
		}
		sentences.remove(sentences.size() - 1);

		ArrayList<Expression> expressions = new ArrayList<Expression>();
		for (int i = 0; i < sentences.size(); i++) {
			Expression exp = parseExpression(sentences.get(i));
			if (exp != null) {
				expressions.add(exp);
			}
		}

		buildModel(expressions, newModel);
	}

	/**
	 * PRE: nodeName and nodes are defined
	 * POST: returns the node object where node.name = name, if it exists .Null
	 * otherwise
	 * 
	 * @param nodeName
	 * @param nodes
	 * @return
	 */
	private Node findNodeByName(String nodeName, ArrayList<Node> nodes) {
		Node nodeSought = null;
		for (int i = 0; i < nodes.size() && nodeSought == null; i++) {
			if (nodes.get(i).getAbrevName().equalsIgnoreCase(nodeName)) {
				nodeSought = nodes.get(i);
			}
		}
		return nodeSought;
	}

	/**
	 * PRE: expressions and newModel are defined
	 * POST: constructs a model in newModel by adding nodes and edges based on
	 * the data in expressions
	 * 
	 * @param expressions
	 * @param newModel
	 */
	private void buildModel(ArrayList<Expression> expressions, Model newModel) {
		ArrayList<Node> nodes = newModel.getNodes();
		ArrayList<Edge> edges = newModel.getEdges();
		ArrayList<Node> start;
		ArrayList<Node> end;
		Edge edge;
		int startX = (newModel.getTotalSize().width - (expressions.size() * NODE_SPACING) / 2) / 2;
		int startY = (newModel.getTotalSize().height - (expressions.size() * NODE_SPACING) / 2) / 2;
		try {
			for (Expression exp : expressions) {
				start = new ArrayList<Node>();
				end = new ArrayList<Node>();

				// add start nodes
				for (String startNodeIter : exp.startNodes) {
					start.add(findNodeByName(startNodeIter, nodes));

					if (start.get(start.size() - 1) == null) {
						start.set(
								start.size() - 1,
								new Node(startNodeIter, startNodeIter,
										new ImageIcon("Resources/Images/node1-"
												+ nodes.size() % 15 + ".png"),
										nodes.size() % 15, SHAPE.CIRCLE,
										new Point(

										startX + (nodes.size() % 5 + 1)
												* NODE_SPACING, startY
												+ (nodes.size() / 5 + 1)
												* NODE_SPACING), 20, 2,
										newModel));
						nodes.add(start.get(start.size() - 1));
					}
				}

				// add end nodes
				for (String endNodeIter : exp.endNodes) {
					end.add(findNodeByName(endNodeIter, nodes));
					if (end.get(end.size() - 1) == null) {
						end.set(end.size() - 1,
								new Node(endNodeIter, endNodeIter,
										new ImageIcon("Resources/Images/node1-"
												+ nodes.size() % 15 + ".png"),
										nodes.size() % 15, SHAPE.CIRCLE,
										new Point(startX
												+ (nodes.size() % 5 + 1)
												* NODE_SPACING, startY
												+ (nodes.size() / 5 + 1)
												* NODE_SPACING), 20, 2,
										newModel));
						nodes.add(end.get(end.size() - 1));
					}
				}

				// add edges
				for (Node startIter : start) {
					for (Node endIter : end) {
						edge = new Edge(startIter, endIter, 0, null, newModel
								.getColors().get(startIter.getColorChoice()),
								startIter.getColorChoice(), exp.edge, newModel,
								false);
						edges.add(edge);
						startIter.addOutEdge(edge);
						endIter.addInEdge(edge);
					}
				}
			}
		} catch (InvalidTableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "is", "was", "were", or "are"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isPassiveVerb(String token) {
		return (token.equals("is") || token.equals("was")
				|| token.equals("were") || token.equals("are"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "by"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isBy(String token) {
		return (token.equals("by"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "of"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isOf(String token) {
		return (token.equals("of"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "does" or "do"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isDoesOrDo(String token) {
		return (token.equals("does") || token.equals("do"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "the" "a" or "an"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isTheAOrAn(String token) {
		return (token.equals("the") || token.equals("a") || token.equals("an"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "not"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isNot(String token) {
		return (token.equals("not"));
	}

	/**
	 * PRE: token is defined
	 * POST: RV = true if token = "not"
	 * 
	 * @param token
	 * @return
	 */
	private boolean isAdjective(String token) {
		return (adjectiveDict.get(token.hashCode()) != null || token
				.equals("with"));
	}

	/**
	 * PRE: token is the first name, and scan contains the next word which may
	 * be a list of names separated by comma's and
	 * ended with 'and' before the last name
	 * POST: all names have been parsed and added to exp.start, token is the
	 * next token, and scan is set to
	 * the first word after token
	 * 
	 * @param exp
	 * @param scan
	 * @return
	 */
	private void parseNames(ArrayList<String> nodes, String token,
			ArrayDeque<String> sentence) {
		nodes.add(token);

		// comma-list?
		boolean andFound = false;
		while (token.endsWith(",") && !andFound) {
			nodes.set(
					nodes.size() - 1,
					nodes.get(nodes.size() - 1).substring(0,
							nodes.get(nodes.size() - 1).length() - 1));
			token = sentence.pop(); // 'and' or additional name

			andFound = isAnd(token);
			if (!andFound) {
				nodes.add(token); // it was an additional name
			}
		}

		if (!sentence.isEmpty() && isAnd(sentence.peek())) {
			sentence.pop();
			andFound = true;
		}

		// if it was multiple names in a list, andFound is true
		if (andFound) {
			token = sentence.pop();
			nodes.add(token);
		}

	}

	private Boolean isAnd(String token) {
		return token.equals("and");
	}

	/**
	 * PRE: sentence is defined
	 * POST: sentence has been parsed as an Expression and that expression is
	 * returned
	 * 
	 * @param expString
	 * @return
	 */
	private Expression parseExpression(ArrayDeque<String> sentence) {
		// System.out.println(expString);
		Expression exp = new Expression();
		String token = "";

		token = sentence.pop();
		parseNames(exp.startNodes, token, sentence);
		token = sentence.pop();

		if (isPassiveVerb(token)) {
			token = sentence.pop();

			// some holes here for plural expressions like
			// "A, B, and C are regulators of D", because passive is assumed
			// after seeing "are" without a 'the', 'a', or 'an'
			if (isTheAOrAn(token)) {
				parseActiveVoiceSubExpression(sentence, token, exp);
			} else {
				exp.endNodes = exp.startNodes;
				exp.startNodes = new ArrayList<String>();
				parsePassiveVoiceSubExpression(sentence, token, exp);
			}
		} else {
			parseActiveVoiceSubExpression(sentence, token, exp);
		}
		return exp;
	}

	/**
	 * PRE: sentence, toke, and exp are defined
	 * POST: sentence has been parsed as an activeVoice expression and the
	 * expression returned
	 * 
	 * @param sentence
	 * @param token
	 * @param exp
	 * @return
	 */
	private Expression parseActiveVoiceSubExpression(
			ArrayDeque<String> sentence, String token, Expression exp) {
		if (isDoesOrDo(token)) {
			token = sentence.pop(); // "not" or action
			if (isNot(token)) {
				token = sentence.pop(); // action
			}
		}
		exp.edge = token;
		token = sentence.pop(); // adjective or node2
		while (isAdjective(token)) {
			token = sentence.pop(); // adjective or node2
		}
		parseNames(exp.endNodes, token, sentence);
		// If there are more words in the expression, the edge name is
		// clearly confusing and the extra words don't change the names of
		// the nodes so just give the edges a default name and be done with
		// it
		while (!sentence.isEmpty()) {
			token = sentence.pop();
			exp.edge = DEFAULT_EDGE_NAME;
			if (isOf(token)) {
				token = sentence.pop();
				exp.endNodes = new ArrayList<String>();
				parseNames(exp.endNodes, token, sentence);
			}
		}

		return exp;
	}

	/**
	 * PRE: sentence, token, and exp are defined, exp already contains a list of
	 * endNodes
	 * POST: sentence has been parsed as a passiveVoice expression and returned
	 * 
	 * @param sentence
	 * @param token
	 * @param exp
	 */
	private void parsePassiveVoiceSubExpression(ArrayDeque<String> sentence,
			String token, Expression exp) {
		if (isNot(token)) {
			token = sentence.pop(); // edge
		}
		exp.edge = token;
		token = sentence.pop(); // "by", adjective, or "to"-CS, which are boils
								// down to "by" or not, but "by" must show up
								// sometime
		while (!isBy(token)) {
			// System.out.println(token);
			exp.edge = DEFAULT_EDGE_NAME;
			token = sentence.pop();
		}
		token = sentence.pop(); // 'cause'-action (A is destroyed by the
								// phosphorylization of B) or node1; assume
								// node1 unless an 'of' is hit later
		parseNames(exp.startNodes, token, sentence);
		while (!sentence.isEmpty()) {
			token = sentence.pop();
			exp.edge = DEFAULT_EDGE_NAME;
			// if token is "of" then the earlier "by" was for an action not
			// the node, so the following token will be the actual node1
			if (isOf(token)) {
				token = sentence.pop();
				exp.startNodes = new ArrayList<String>();
				parseNames(exp.startNodes, token, sentence);
			}

		}

	}

	/**
	 * private class to hold a list of start nodes names, end node names, and an
	 * edge name. Used like a struct
	 * 
	 * @author plvines
	 * 
	 */
	private class Expression {
		String edge;
		ArrayList<String> startNodes, endNodes;

		public Expression() {
			startNodes = new ArrayList<String>();
			endNodes = new ArrayList<String>();
			edge = "";
		}

		public String toString() {
			return startNodes + " " + edge + " " + endNodes;
		}
	}

}
