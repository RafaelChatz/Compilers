import java.io.IOException;
import java.io.InputStream;

public class Simple_Calculator {

    private int lookaheadToken;

    private InputStream in;

    public Simple_Calculator(InputStream in) throws IOException {
        this.in = in;
        lookaheadToken = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookaheadToken != symbol)
            throw new ParseError();
        lookaheadToken = in.read();
    }

    private void Expr() throws IOException, ParseError {
        Term();
        Expr2();
    }

    private void Term() throws IOException, ParseError{
        Interc();
        Term2();
    }

    private void Expr2() throws IOException, ParseError{
        if(lookaheadToken == ')'||lookaheadToken == '\n' || lookaheadToken==-1){
            return;
        }

        if(lookaheadToken != '^')
            throw new ParseError();
        consume('^');
        Term();
        Expr2();
    }


    private void Term2() throws IOException , ParseError {
        if(lookaheadToken == '^'||lookaheadToken == ')'||lookaheadToken == '\n' || lookaheadToken==-1){
            return;
        }

        if(lookaheadToken != '&')
            throw new ParseError();
        consume('&');
        Interc();
        Term2();
    }

    private void Interc() throws IOException, ParseError{
        if(lookaheadToken == '\n' || lookaheadToken==-1){
            return;
        }

        if((lookaheadToken < '0' || lookaheadToken > '9') && lookaheadToken!='(')
            throw new ParseError();

        if(lookaheadToken!='('){
            consume(lookaheadToken);
            return;
        }
        consume('(');
        Expr();
        consume(')');
    }


    public void parse() throws IOException, ParseError {
        Expr();
        if (lookaheadToken != '\n' && lookaheadToken != -1)
            throw new ParseError();
    }

    public static void main(String[] args) {

        try {
            Simple_Calculator parser = new Simple_Calculator(System.in);
            parser.parse();
            System.out.print("OK");

        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        catch(ParseError err){
            System.err.println(err.getMessage());
        }

    }
}
