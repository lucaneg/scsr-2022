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

import java.util.Objects;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.SATISFIED;
import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.UNKNOWN;
import static java.lang.Character.isLetter;
import static java.lang.Math.abs;


public class Suffisso extends BaseNonRelationalValueDomain<Suffisso> {


    private static final Suffisso TOP = new Suffisso("");
    private static final Suffisso BOTTOM = new Suffisso(null);
    private String s;
    public Suffisso(String s) {
        this.s= s;
    }
    public Suffisso(){
        this.s=null;
        //
    }

    public static String InvStringa(String s){
        char ch[]=s.toCharArray();
        String inv="";
        for(int i=ch.length-1;i>=0;i--){
            inv=inv+ch[i];
        }
        return inv;
    }

    protected Suffisso lubAux(Suffisso other) throws SemanticException {
      int ldiff=abs(other.s.length() -this.s.length());
       String suff="";
        boolean blocco=true;

         if(this.s.equals("") || other.s.equals(""))
            return new Suffisso("");

         if(Smaller(this,other)==this){
             for(int i=(Smaller(this,other)).s.length()-1;i>0 && blocco;i--){
                 // if(this.s.charAt(i)==other.s.charAt(i+ldiff))
                 if(this.s.charAt(i)==other.s.charAt(i))
                     suff=suff+this.s.charAt(i);
                 else
                     blocco=false;
             }
         }
        if(Smaller(this,other)==other){
            for(int i=(Smaller(this,other)).s.length()-1;i>0 && blocco;i--){
                // if(other.s.charAt(i)==this.s.charAt(i+ldiff))
                if(other.s.charAt(i)==this.s.charAt(i))
                    suff=suff+this.s.charAt(i);
                else
                    blocco=false;
            }
        }
        String invertita=InvStringa(suff);
        return new Suffisso(suff);
    }

    protected Suffisso Smaller(Suffisso uno, Suffisso due){
        int unol =uno.s.length();
        int duel= due.s.length();
        if(unol<duel)
            return uno;
        else return due;
    }

    protected Suffisso Bigger(Suffisso uno, Suffisso due){
        int unol =uno.s.length();
        int duel= due.s.length();
        if(unol<duel)
            return due;
        else return uno;
    }

    @Override
    protected Suffisso glbAux(Suffisso other) throws SemanticException {
        if(this.lessOrEqualAux(other)==false)
            return bottom();
        return Smaller(this, other);
    }

    @Override
    protected Suffisso wideningAux(Suffisso other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(Suffisso other) throws SemanticException {
        boolean trovato=true;
        if(this.s.equals("") || other.s.equals(""))
            return false;
        else {
        for(int i=0;i<other.s.length() && trovato;i++) {
            if(this.s.charAt(this.s.length()-i)!=other.s.charAt(other.s.length()-i)){
                trovato=false;
                }
            }
        }
        return trovato;
    }


    protected Suffisso evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof String) {
            String a = (String)constant.getValue();
            return new Suffisso(a);
        }
        return top();
    }

    @Override
    protected Suffisso evalUnaryExpression(UnaryOperator operator, Suffisso arg, ProgramPoint pp) throws SemanticException {
        return super.evalUnaryExpression(operator, arg, pp);
    }

    @Override
    protected Suffisso evalBinaryExpression(BinaryOperator operator, Suffisso left, Suffisso right, ProgramPoint pp) throws SemanticException {
       if(operator instanceof AdditionOperator || operator instanceof AdditionOperator || operator instanceof Multiplication || operator instanceof DivisionOperator)
            return top();
        else if(operator instanceof StringConcat) {
            return right;// suffisso
        } else  return top();
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Suffisso left, Suffisso right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof StringContains)
                if(left.s.contains(right.s))
                    return SemanticDomain.Satisfiability.SATISFIED;

        return SemanticDomain.Satisfiability.UNKNOWN;
    }



    @Override
    protected Suffisso evalTernaryExpression(TernaryOperator operator, Suffisso left, Suffisso middle, Suffisso right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof StringSubstring) {
            String vuoto="";
            return new Suffisso(vuoto);
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
        Suffisso other = (Suffisso) obj;
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
    public Suffisso top() {
        return TOP;
    }

    @Override
    public Suffisso bottom() {
        return BOTTOM;
    }
}
