package interpreter;

public abstract class Token {
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
