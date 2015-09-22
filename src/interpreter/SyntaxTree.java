package interpreter;

public abstract class SyntaxTree<T extends Token> {
    private final T token;

    public SyntaxTree(T token) {
        this.token = token;
    }

    public T getToken() {
        return token;
    }

    public abstract int eval();

    public static class Leaf extends SyntaxTree<Token.Value> {
        public Leaf(Token.Value value) {
            super(value);
        }

        public int eval() {
            return getToken().getValue();
        }
    }

    public static abstract class Branch<B extends Token.Operator>
            extends SyntaxTree<B> {

        private SyntaxTree left;
        private SyntaxTree right;

        public Branch(B op, SyntaxTree left, SyntaxTree right) {
            super(op);
        }

        public int eval() {
            return getToken().eval(left.eval(), right.eval());
        }

        public static class Add extends Branch<Token.Operator.Add> {
            public Add(SyntaxTree left, SyntaxTree right) {
                super(new Token.Operator.Add(), left, right);
            }
        }

        public static class Mod extends Branch<Token.Operator.Mod> {
            public Mod(SyntaxTree left, SyntaxTree right) {
                super(new Token.Operator.Mod(), left, right);
            }
        }

        public static class Max extends Branch<Token.Operator.Max> {
            public Max(SyntaxTree left, SyntaxTree right) {
                super(new Token.Operator.Max(), left, right);
            }
        }
    }
}

