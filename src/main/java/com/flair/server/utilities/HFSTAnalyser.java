package com.flair.server.utilities;

import com.drew.lang.annotations.NotNull;
import fi.seco.hfst.Transducer;
import fi.seco.hfst.TransducerAlphabet;
import fi.seco.hfst.TransducerHeader;
import fi.seco.hfst.UnweightedTransducer;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;
import static com.flair.server.utilities.HFSTAnalysis.analyseWordForm;

public class HFSTAnalyser {
    private static final String NEWLINE = System.lineSeparator();
    private Transducer transducer;

    /**
     * constructor that also builds a Transducer on the provided {@code InputStream}
     * @param iStream data used to construct a Transducer
     * @throws TransducerStreamException the provided InputStream could not be used to construct a Transducer
     */
    public HFSTAnalyser(@NotNull InputStream iStream) throws TransducerStreamException{
        //TODO: fix the construction of the TransducerHeader
        /*try {
            DataInputStream dataStream = new DataInputStream(iStream);
            TransducerHeader transducerHeader = new TransducerHeader(dataStream);
            this.transducer = new UnweightedTransducer(dataStream, transducerHeader, new TransducerAlphabet(dataStream, transducerHeader.getSymbolCount()));
        } catch (IOException e) {
            ServerLogger.get().error(e, "InputStream could not be used to construct a Transducer");
            throw new TransducerStreamException("HFSTAnalyser not able to construct Transducer from InputStream");
        }*/
    }

    /**
     * Run the transducer meant for multiple elements.
     *
     * @param wordFormList the list of word forms
     * @return a string with tab delimited word form and a lemma combined
     * with "+" delimited string of features for each reading per line
     * while the analyses of each word form is separated by a new line.
     */
    public String runTransducer(Collection<String> wordFormList) {
        return wordFormList.stream()
                .map(token -> analyseWordForm(this.transducer, token))
                .collect(Collectors.joining(NEWLINE));
    }

    /**
     * Run the transducer meant for a single element.
     *
     * @param wordForm the word form
     * @return a string with tab delimited word form and a lemma combined
     * with "+" delimited string of features for each reading per line
     */
    public String runTransducer(String wordForm) {
        return analyseWordForm(this.transducer, wordForm);
    }

    public static class TransducerStreamException extends Exception{
        public TransducerStreamException() {
            super();
        }
        public TransducerStreamException(Throwable throwable) {
            super(throwable);
        }
        protected TransducerStreamException(String s, Throwable throwable, boolean b, boolean b1) {
            super(s, throwable, b, b1);
        }
        public TransducerStreamException(String s) {
            super(s);
        }
    }
}
