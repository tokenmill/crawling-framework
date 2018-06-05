package lt.tokenmill.crawling.parser.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class TextProfileSignature {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private float quantRate;
    private float minTokenLen;
    private MessageDigest digester;

    public TextProfileSignature() {
        this.quantRate = 0.01f;
        this.minTokenLen = 2;
        try {
            this.digester = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            LOG.error("Failed to initialize Media digest algorithm");
            throw new RuntimeException("Failed to initialize Media digest algorithm", e);
        }
    }

    public String getSignature(String text) {
        add(text);
        byte[] digest = digester.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String signature = bigInt.toString(16);
        digester.reset();
        return signature;
    }

    public void add(String content) {
        HashMap<String, Token> tokens = new HashMap<>();

        StringBuilder curToken = new StringBuilder();
        int maxFreq = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                curToken.append(Character.toLowerCase(c));
            } else {
                if (curToken.length() > 0) {
                    if (curToken.length() > minTokenLen) {
                        // add it
                        String s = curToken.toString();
                        Token tok = tokens.get(s);
                        if (tok == null) {
                            tok = new Token(0, s);
                            tokens.put(s, tok);
                        }
                        tok.cnt++;
                        if (tok.cnt > maxFreq)
                            maxFreq = tok.cnt;
                    }
                    curToken.setLength(0);
                }
            }
        }
        // check the last token
        if (curToken.length() > minTokenLen) {
            // add it
            String s = curToken.toString();
            Token tok = tokens.get(s);
            if (tok == null) {
                tok = new Token(0, s);
                tokens.put(s, tok);
            }
            tok.cnt++;
            if (tok.cnt > maxFreq)
                maxFreq = tok.cnt;
        }
        Iterator<Token> it = tokens.values().iterator();
        ArrayList<Token> profile = new ArrayList<>();
        // calculate the QUANT value
        int quant = Math.round(maxFreq * quantRate);
        if (quant < 2) {
            if (maxFreq > 1)
                quant = 2;
            else
                quant = 1;
        }
        while (it.hasNext()) {
            Token t = it.next();
            // round down to the nearest QUANT
            t.cnt = (t.cnt / quant) * quant;
            // discard the frequencies below the QUANT
            if (t.cnt < quant) {
                continue;
            }
            profile.add(t);
        }
        profile.sort(new TokenComparator());
        StringBuilder newText = new StringBuilder();
        it = profile.iterator();
        while (it.hasNext()) {
            Token t = it.next();
            if (newText.length() > 0)
                newText.append("\n");
            newText.append(t.toString());
        }
        digester.update(newText.toString().getBytes());
    }

    private static class Token {
        public int cnt;
        public String val;

        public Token(int cnt, String val) {
            this.cnt = cnt;
            this.val = val;
        }

        @Override
        public String toString() {
            return val + " " + cnt;
        }
    }

    private static class TokenComparator implements Comparator<Token> {
        @Override
        public int compare(Token t1, Token t2) {
            return t2.cnt - t1.cnt;
        }
    }
}
