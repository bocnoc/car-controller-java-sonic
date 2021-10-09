package Util;

public class Option {
    final String shortStr;
    final String longStr;
    final String hint;
    final boolean hasValue;
    String value;

    public Option(final String option, final String longOption, final String hint, final boolean hasValue) {
        this.shortStr = option;
        this.longStr = longOption;
        this.hint = hint;
        this.hasValue = hasValue;
    }

    public String getValue() {
        return value;
    }
}
