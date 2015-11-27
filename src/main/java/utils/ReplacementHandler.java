package utils;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

public class ReplacementHandler extends Reader {

    protected PushbackReader pushbackReader = null;
    protected TokenDeterminer tokenResolver = null;
    protected StringBuilder tokenNameBuffer = new StringBuilder();
    protected String tokenValue = null;
    protected int tokenValueIndex = 0;

    public ReplacementHandler(Reader source, TokenDeterminer resolver) {
        this.pushbackReader = new PushbackReader(source, 2);
        this.tokenResolver = resolver;
    }

    public int read() throws IOException {
        if(this.tokenValue != null){
            if(this.tokenValueIndex < this.tokenValue.length()){
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if(this.tokenValueIndex == this.tokenValue.length()){
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }

        int data = this.pushbackReader.read();
        if(data != '(') return data;
        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());
        data = this.pushbackReader.read();
        while(data != ')'){
            this.tokenNameBuffer.append((char) data);
            data = this.pushbackReader.read();
        }

        this.tokenValue = this.tokenResolver
                .resolveToken(this.tokenNameBuffer.toString());

        if(this.tokenValue == null){
            this.tokenValue = "("+ this.tokenNameBuffer.toString() + ")";
        }
        if(this.tokenValue.length() == 0){
            return read();
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);
    }

    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    public int read(char charBuffer[], int off, int len) throws IOException {
        int currentChar = 0;
        for(int i=0; i<len; i++){
            int nextChar = read();
            if(nextChar == -1) {
                if(currentChar == 0){
                    currentChar = -1;
                }
                break;
            }
            currentChar = i + 1;
            charBuffer[off + i] = (char) nextChar;
        }
        return currentChar;
    }

    public void close() throws IOException {
        this.pushbackReader.close();
    }
}
