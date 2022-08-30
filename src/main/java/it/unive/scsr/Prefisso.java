package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.Multiplication;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.lang.invoke.StringConcatFactory;
import java.util.Objects;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.SATISFIED;
import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.UNKNOWN;

public class Prefisso extends BaseNonRelationalValueDomain<Prefisso> {

    private static final Prefisso TOP = new Prefisso("");
    private static final Prefisso BOTTOM = new Prefisso(null);
    private String s;
    public Prefisso(String s) {
        this.s=s;
    }
    public Prefisso(){
        this(null);
    }

    @Override
    protected Prefisso lubAux(Prefisso other) throws SemanticException {
        String comune="";
        for(int i=0;i<other.s.length() && i<this.s.length(); i++){
            if(this.s.charAt(i)==other.s.charAt(i))
                comune=comune + other.s.charAt(i);
            else
                i=other.s.length();
        }
            if(comune!="")
                return new Prefisso(comune);
            else
                return top();
    }

    @Override
    public Prefisso glb(Prefisso other) throws SemanticException {
        if(lessOrEqualAux(other)==true)
            return this;
        else if(other.lessOrEqualAux(this)==true) {
            return other;
        } else return bottom();
    }

    @Override
    protected Prefisso wideningAux(Prefisso other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(Prefisso other) throws SemanticException {
        if(this.s.length()>=other.s.length()){
            boolean trovato=true;
            for(int i=0;i<other.s.length()-1 && trovato ;i++){
                if(this.s.charAt(i)!=other.s.charAt(i))
                    trovato=false;
            }
            return trovato;
        }
        return false;
    }

    @Override
    protected Prefisso evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof String) {
            String a = (String)constant.getValue();
            return new Prefisso(a);
        }
        return top();
    }
    @Override
    protected Prefisso evalUnaryExpression(UnaryOperator operator, Prefisso arg, ProgramPoint pp) throws SemanticException {
        return super.evalUnaryExpression(operator, arg, pp);
    }

    @Override
    protected Prefisso evalBinaryExpression(BinaryOperator operator, Prefisso left, Prefisso right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof AdditionOperator || operator instanceof AdditionOperator || operator instanceof Multiplication || operator instanceof DivisionOperator)
            return top();
        else if(operator instanceof StringConcat) {
            return left;// prefisso
        } else  return top();
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Prefisso left, Prefisso right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof StringContains)
               if(this.s.contains(right.s))
                 return SemanticDomain.Satisfiability.SATISFIED;

       return SemanticDomain.Satisfiability.UNKNOWN;


    }

    @Override
    protected Prefisso evalTernaryExpression(TernaryOperator operator, Prefisso left, Prefisso middle, Prefisso right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof StringSubstring) {
            String pref="";
            int a=Integer.parseInt(middle.s);
            int b=Integer.parseInt(right.s);
            if(b<= left.s.length()){
               return new Prefisso(left.s.substring(a,b));
            }else if(b>left.s.length() && a<left.s.length()){
                    return new Prefisso(left.s.substring(a,left.s.length()-1));
            }else {
                String vuoto="";
                    return new Prefisso(vuoto);
            }
        }
        return top();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Prefisso other = (Prefisso) obj;
        return this.s == other.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(s);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(s);
    }

    @Override
    public Prefisso top() {
        return TOP;
    }

    @Override
    public Prefisso bottom() {
        return BOTTOM;
    }
}
