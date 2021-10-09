package Util;

import java.util.HashMap;

public class OptionParser {

    final HashMap<String, Option> preDefinedOption;
    final HashMap<String, Option> loadedOption;

    public OptionParser() {
        this.preDefinedOption = new HashMap<>();
        this.loadedOption = new HashMap<>();
    }

    public void addOption(final Option option) {
        if (option.longStr != null) {
            preDefinedOption.put(option.longStr, option);
        }
        preDefinedOption.put(option.shortStr, option);
    }

    public void parseArgs(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            final var opt = preDefinedOption.get(args[i]);
            if (opt == null) {
                throw new IllegalArgumentException(String.format("[%s]: Unknown Option", args[i]));
            }
            if (opt.hasValue) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(String.format("[%s]: parameter should be given", args[i]));
                }
                opt.value = args[i++];
            }
            loadedOption.put(opt.shortStr, opt);
            if (opt.longStr != null) {
                loadedOption.put(opt.longStr, opt);
            }
        }
    }

    public Option getOption(String option) {
        return loadedOption.get(option);
    }
}
