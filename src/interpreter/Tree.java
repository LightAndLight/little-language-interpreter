package interpreter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tree {
	private Token token;
	private Tree left;
	private Tree right;

	public Tree(String s) throws UnexpectedInputException {
		Tree tree = new Parser().parseString(new Lexer().tokenize(s));
		left = tree.getLeft();
		right = tree.getRight();
		token = tree.getToken();
	}

	private Tree(Token token, Tree left, Tree right) {
		this.token = token;
		this.left = left;
		this.right = right;
	}

	private Tree(Token token) {
		this.token = token;
		this.left = null;
		this.right = null;
	}

	private Token getToken() {
		return token;
	}

	private Tree getLeft() {
		return left;
	}

	private Tree getRight() {
		return right;
	}

	private boolean isLeaf() {
		return right == null && left == null;
	}

	private boolean isBranch() {
		return right != null && left != null;
	}

	private Tree copy() {
		if (isLeaf()) {
			return this;
		} else {
			return new Tree(getToken(), left.copy(), right.copy());
		}
	}

	private void reduce() {
		if (isBranch()) {
			int left = ((Token.Value) this.left.getToken()).getValue();
			int right = ((Token.Value) this.right.getToken()).getValue();
			Token.Operator op = (Token.Operator) token;

			token = new Token.Value(op.evaluate(left, right));
			this.left = null;
			this.right = null;
		}
	}

	private String toStringNested() {
		if (isLeaf()) {
			return getToken().toString();
		} else {
			return "(" + this.toString() + ")";
		}
	}


	@Override
	public String toString() {
		if (isLeaf()) {
			return getToken().toString();
		} else {
			return getLeft().toStringNested() + " "
					+ getToken().toString() + " "
					+ getRight().toStringNested();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Tree) {
			Tree tree = (Tree)o;
				return (tree.isLeaf()
						&& this.isLeaf()
						&& this.getToken().equals(tree.getToken()))
						|| (tree.isBranch()
						&& this.isBranch()
						&& left.equals(tree.getLeft())
						&& right.equals(tree.getRight())
						&& token.equals(tree.getToken()));
		} else {
			return false;
		}
	}

	public static class UnexpectedInputException extends Exception {
		public UnexpectedInputException(String message) {
			super(message);
		}
	}

	public static class Lexer {
		private static abstract class TokenFinder {
			private final Pattern pattern;

			public TokenFinder(Pattern pattern) {
				this.pattern = pattern;
			}

			public Matcher getMatcher(String s) {
				return pattern.matcher(s);
			}

			public abstract Optional<Token> getToken(String s);

			public static class OpenParenFinder extends TokenFinder {
				public OpenParenFinder() {
					super(Pattern.compile("^\\("));
				}

				public Optional<Token> getToken(String s) {
					if (getMatcher(s).find(0)) {
						return Optional.of(new Token.OpenParen());
					} else {
						return Optional.empty();
					}
				}
			}

			public static class CloseParenFinder extends TokenFinder {
				public CloseParenFinder() {
					super(Pattern.compile("^\\)"));
				}

				public Optional<Token> getToken(String s) {
					if (getMatcher(s).find(0)) {
						return Optional.of(new Token.CloseParen());
					} else {
						return Optional.empty();
					}
				}
			}

			public static class ValueFinder extends TokenFinder {
				public ValueFinder() {
					super(Pattern.compile("^\\d+"));
				}

				public Optional<Token> getToken(String s) {
					Matcher matcher = getMatcher(s);
					if (matcher.find(0)) {
						return Optional.of(new Token.Value(Integer.parseInt(matcher.group())));
					} else {
						return Optional.empty();
					}
				}
			}

			public static class ModFinder extends TokenFinder {
				public ModFinder() {
					super(Pattern.compile("^\\s%\\s"));
				}

				public Optional<Token> getToken(String s) {
					if (getMatcher(s).find(0)) {
						return Optional.of(new Token.Operator.Mod());
					} else {
						return Optional.empty();
					}
				}
			}

			public static class MaxFinder extends TokenFinder {
				public MaxFinder() {
					super(Pattern.compile("^\\s\\?\\s"));
				}

				public Optional<Token> getToken(String s) {
					if (getMatcher(s).find(0)) {
						return Optional.of(new Token.Operator.Max());
					} else {
						return Optional.empty();
					}
				}
			}

			public static class AddFinder extends TokenFinder {
				public AddFinder() {
					super(Pattern.compile("\\s&\\s"));
				}

				public Optional<Token> getToken(String s) {
					if (getMatcher(s).find()) {
						return Optional.of(new Token.Operator.Add());
					} else {
						return Optional.empty();
					}
				}
			}
		}

		private final TokenFinder[] tokenFinders = {
				new TokenFinder.OpenParenFinder(),
				new TokenFinder.CloseParenFinder(),
				new TokenFinder.ValueFinder(),
				new TokenFinder.MaxFinder(),
				new TokenFinder.ModFinder(),
				new TokenFinder.AddFinder()
		};

		public LinkedList<Token> tokenize(String input) throws UnexpectedInputException {
			LinkedList<Token> result = new LinkedList<>();
			String remaining = input;
			while (!remaining.isEmpty()) {
				Optional<Token> maybeToken = Optional.empty();
				for (TokenFinder tokenFinder : tokenFinders) {
					maybeToken = tokenFinder.getToken(remaining);
					if (maybeToken.isPresent()) {
						result.addLast(maybeToken.get());
						remaining = tokenFinder.getMatcher(remaining).replaceFirst("");
						break;
					}
				}

				if (!remaining.isEmpty() && !maybeToken.isPresent()) {
					throw new UnexpectedInputException("Unexpected input: " + remaining.substring(0, 1));
				}
			}
			return result;
		}
	}

	public static class Parser {
		// LL(1)
		// 1.	S  -> A
		// 2.	A  -> T A*
		// 3.	A* -> % T | ? T | & T | e
		// 4.	T  -> (A) | number

		public Tree parseString(LinkedList<Token> input) throws UnexpectedInputException {
			Optional<Tree> maybeLeftSide = parseLeftSide(input);
			if (maybeLeftSide.isPresent()) {
				return maybeLeftSide.get();
			} else {
				throw new UnexpectedInputException("Unexpected input");
			}
		}

		private boolean parseToken(LinkedList<Token> input, Token.Type type) {
			if (input.getFirst().getType() == type) {
				input.removeFirst();
				return true;
			} else {
				return false;
			}
		}

		private Optional<Tree> parseValue(LinkedList<Token> input) {
			Token token = input.getFirst();
			if (parseToken(input, Token.Type.VALUE)) {
				return Optional.of(new Tree(token));
			} else {
				return Optional.empty();
			}
		}

		private Optional<Tree> parseLeftSide(LinkedList<Token> input) {
			return parseTerminal(input).map(
					terminal -> parseRightSide(input).map(
							tree -> new Tree(tree.getToken(), terminal, tree.getRight())
					).orElse(terminal)
			);
		}

		private Optional<Tree> parseTerminal(LinkedList<Token> input) {
			Optional<Tree> maybeBracketed = parseBracketed(input);
			if (maybeBracketed.isPresent()) {
				return maybeBracketed;
			} else {
				return parseValue(input);
			}
		}


		private Optional<Tree> parseRightSide(LinkedList<Token> input) {
			Optional<Tree> maybeMax = parseMax(input);
			if (maybeMax.isPresent()) {
				return maybeMax;
			}

			Optional<Tree> maybeMod = parseMod(input);
			if (maybeMod.isPresent()) {
				return maybeMod;
			}

			return parseAdd(input);
		}

		private Optional<Tree> parseBracketed(LinkedList<Token> input) {
			if (parseToken(input, Token.Type.OPEN_PAREN)) {
				Optional<Tree> maybeLeftSide = parseLeftSide(input);
				if (parseToken(input, Token.Type.CLOSE_PAREN)) {
					return maybeLeftSide;
				}
			}

			return Optional.empty();
		}

		private Optional<Tree> parseMax(LinkedList<Token> input) {
            if (input.size() >= 2 && parseToken(input, Token.Type.MAX)) {
				return parseTerminal(input).map(terminal -> new Tree(new Token.Operator.Max(), null, terminal));
			} else {
				return Optional.empty();
			}
		}

		private Optional<Tree> parseMod(LinkedList<Token> input) {
			if (input.size() >= 2 && parseToken(input, Token.Type.MOD)) {
				return parseTerminal(input).map(terminal -> new Tree(new Token.Operator.Mod(), null, terminal));
			} else {
				return Optional.empty();
			}
		}

		private Optional<Tree> parseAdd(LinkedList<Token> input) {
			if (input.size() >= 2 && parseToken(input, Token.Type.ADD)) {
				return parseTerminal(input).map(terminal -> new Tree(new Token.Operator.Add(), null, terminal));
			} else {
				return Optional.empty();
			}
		}
	}

	public static abstract class Token {
		enum Type {
			MAX,
			MOD,
			ADD,
			VALUE,
			OPEN_PAREN,
			CLOSE_PAREN
		}

		public abstract Type getType();

		@Override
		public abstract boolean equals(Object o);

		public static abstract class Operator extends Token {
			@Override
			public boolean equals(Object o) {
				return o.getClass().equals(this.getClass());
			}

			@Override
			public abstract String toString();

			public abstract int evaluate(int val1, int val2);

			public static class Add extends Token.Operator {
				public Type getType() {
					return Type.ADD;
				}

				public int evaluate(int val1, int val2) {
					return val1 + val2;
				}

				public String toString() {
					return "&";
				}
			}

			public static class Mod extends Token.Operator {
				public Type getType() {
					return Type.MOD;
				}

				public int evaluate(int val1, int val2) {
					return val1 % val2;
				}

				public String toString() {
					return "%";
				}
			}

			public static class Max extends Token.Operator {
				public Type getType() {
					return Type.MAX;
				}

				public int evaluate(int val1, int val2) {
					return Integer.max(val1, val2);
				}

				public String toString() {
					return "?";
				}
			}
		}

		public static class Value extends Token {
			private final int value;
			public Value(int value) {
				this.value = value;
			}

			public int getValue() {
				return value;
			}

			public Type getType() {
				return Type.VALUE;
			}

			@Override
			public boolean equals(Object o) {
                return o instanceof Value && value == ((Value)o).getValue();
			}

			@Override
			public String toString() {
				return Integer.toString(getValue());
			}
		}

		public static class OpenParen extends Token {
			public Type getType() {
				return Type.OPEN_PAREN;
			}

			@Override
			public boolean equals(Object o) {
				return o instanceof OpenParen;
			}

		}

		public static class CloseParen extends Token {
			public Type getType() {
				return Type.CLOSE_PAREN;
			}

			@Override
			public boolean equals(Object o) {
				return o instanceof CloseParen;
			}
		}
	}

	/**
	 * Return the height of the tree
	 *
	 * Note: Height is defined as the number of nodes in the longest path
	 * from the root node to a leaf node. (1 node = height 0, 2 layers = height 1)
	 */
    public int getHeight() {
		if (isLeaf()) {
			return 0;
		} else {
			return 1 + Integer.max(getLeft().getHeight(), getRight().getHeight());
		}
    }

	/**
	 * Return the number of leaves in the tree
	 */
	public int nbLeaves() {
		if (isLeaf()) {
			return 1;
		} else {
			return left.nbLeaves() + right.nbLeaves();
		}
	}

	/**
	 * Return the result of evaluating each operation in the tree
	 * using recursion.
	 */
	public int getResultRec() {
		if (isLeaf()) {
			return ((Token.Value)getToken()).getValue();
		} else {
			return ((Token.Operator)getToken()).evaluate(getLeft().getResultRec(), getRight().getResultRec());
		}
	}

	/**
	 * Return a list in which each item is a step in evaluating the tree.
	 */
	public List<String> getResultBySteps() {
        Deque<Tree> treeStack = new LinkedList<>();
		List<String> steps = new LinkedList<>();
		Tree current;
		Tree previous = null;
		Tree copy;

        copy = this.copy();

		treeStack.push(copy);
		steps.add(copy.toString());

		while (!treeStack.isEmpty()) {
			current = treeStack.peek();

			if (previous == null || current == previous.getLeft() || current == previous.getRight()) {
				if (current.getLeft() != null) {
					treeStack.push(current.getLeft());
				} else if (current.getRight() != null) {
					treeStack.push(current.getRight());
				}
			} else if (previous == current.getLeft()) {
				if (current.getRight() != null) {
					treeStack.push(current.getRight());
				}
			} else if (previous == current.getRight()) {
				current.reduce();
				steps.add(copy.toString());
			} else {
				treeStack.pop();
			}

			previous = current;
		}

		return steps;
	}

	/**
	 * Return the result of evaluating each operation in the tree
	 * using an iterative method.
	 */
	public int getResultIt() {
		Deque<Tree> treeStack = new LinkedList<>();
		Deque<Integer> resultStack = new LinkedList<>();
		Tree current;
		Tree previous = null;

		treeStack.push(this);

		while (!treeStack.isEmpty()) {
			current = treeStack.peek();

			if (previous == null || current == previous.getLeft() || current == previous.getRight()) {
				if (current.getLeft() != null) {
					treeStack.push(current.getLeft());
				} else if (current.getRight() != null) {
					treeStack.push(current.getRight());
				}
			} else if (previous == current.getLeft()) {
				if (current.getRight() != null) {
					treeStack.push(current.getRight());
				}
			} else {
				Token currentToken = current.getToken();
				if (currentToken.getType() == Token.Type.VALUE) {
					resultStack.push(((Token.Value)currentToken).getValue());
				} else {
					Token.Operator op = (Token.Operator)currentToken;
					int right = resultStack.pop();
					int left = resultStack.pop();
					resultStack.push(op.evaluate(left, right));
				}
				treeStack.pop();
			}

			previous = current;
		}

		return resultStack.pop();
	}
}

