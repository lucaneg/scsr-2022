package it.unive.scsr;


import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;

import it.unive.lisa.symbolic.value.Constant;

import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;


import java.util.*;

import static it.unive.lisa.analysis.SemanticDomain.Satisfiability.*;

public class CharacterInclusion extends BaseNonRelationalValueDomain<CharacterInclusion> {

    /*private String[] Contenuti;
    private String[] forseContenuti;

    private static String[] topArray={"a","b","c","d","e","f","g","h","i","j",
    "k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    private static final CharacterInclusion TOP = new CharacterInclusion (null,topArray);
    private static final CharacterInclusion BOTTOM = new CharacterInclusion(null,null);
    public CharacterInclusion(String[] Contenuti,String[] forseContenuti){
        this.Contenuti=Contenuti;
        this.forseContenuti=forseContenuti;
    }
    public CharacterInclusion(){
        this.Contenuti=null;
        this.forseContenuti=topArray;
    }*/
    private Set<Character> Contenuti;
    private Set<Character> forseContenuti;
    private static Set<Character> topSet = new HashSet<>(Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'));
    private static final CharacterInclusion TOP = new CharacterInclusion (null,topSet);
    private static final CharacterInclusion BOTTOM = new CharacterInclusion(null,null);
    public CharacterInclusion() {
        this(null, topSet);
    }
    public CharacterInclusion(Set<Character> Contenuti, Set<Character> forseContenuti) {
        this.Contenuti = Contenuti;
        this.forseContenuti = forseContenuti;
    }
    @Override
    public CharacterInclusion top() {return TOP;}

    @Override
    public CharacterInclusion bottom() {return BOTTOM;}

    @Override
    public DomainRepresentation representation() {return new StringRepresentation(Contenuti+" "+forseContenuti);}

    @Override
    protected CharacterInclusion lubAux(CharacterInclusion other) throws SemanticException {
         //https://www.javacodegeeks.com/2020/10/java-program-to-get-union-of-two-arrays.html#:~:text=We%20can%20do%20the%20union,both%20numbers%20and%20string%20values.
         /*Set<String> intersezione = new HashSet<>();
         intersezione.addAll(Arrays.asList(this.Contenuti));
         intersezione.retainAll(Arrays.asList(other.Contenuti));
         String[] arrayIntersezione={};
         arrayIntersezione=intersezione.toArray(arrayIntersezione);
         Set<String> unione = new HashSet<>();
         unione.addAll(Arrays.asList(this.forseContenuti));
         unione.addAll(Arrays.asList(other.forseContenuti));
         String[] arrayUnione={};
         arrayUnione=unione.toArray(arrayUnione);
         return new CharacterInclusion(arrayIntersezione,arrayUnione);*/
        Set<Character> intersezione = new HashSet<>();
        intersezione.addAll(this.Contenuti);
        intersezione.retainAll(other.Contenuti);
        Set<Character> unione = new HashSet<>();
        unione.addAll(this.forseContenuti);
        unione.addAll(other.forseContenuti);
        return new CharacterInclusion(intersezione,unione);
    }

    @Override
    protected CharacterInclusion glbAux(CharacterInclusion other) throws SemanticException {
        /*int Lothercont=other.Contenuti.length;
        int Lthisforsecont=this.forseContenuti.length;
        boolean condizione1=true;
        boolean condizione2=true;
        for(int i=0;i<Lothercont && condizione1;i++){
            //https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
            if(!Arrays.asList(other.forseContenuti).contains(this.Contenuti[i]))
                condizione1=false;
        }
        for(int i=0;i<Lthisforsecont && condizione2;i++){
            if(!Arrays.asList(this.forseContenuti).contains(other.Contenuti[i]))
                condizione2=false;
        }
        if (condizione1 && condizione2){
            Set<String> unione = new HashSet<>();
            unione.addAll(Arrays.asList(this.Contenuti));
            unione.addAll(Arrays.asList(other.Contenuti));
            String[] arrayUnione={};
            arrayUnione=unione.toArray(arrayUnione);
            Set<String> intersezione = new HashSet<>();
            intersezione.addAll(Arrays.asList(this.forseContenuti));
            intersezione.retainAll(Arrays.asList(other.forseContenuti));
            String[] arrayIntersezione={};
            arrayIntersezione=intersezione.toArray(arrayIntersezione);
            return new CharacterInclusion(arrayUnione,arrayIntersezione);
        }else
            return bottom();*/
        if((other.forseContenuti.containsAll(this.Contenuti)) && (this.forseContenuti.containsAll(other.Contenuti))){
            Set<Character> unione = new HashSet<>();
            unione.addAll(this.Contenuti);
            unione.addAll(other.Contenuti);
            Set<Character> intersezione = new HashSet<>();
            intersezione.addAll(this.forseContenuti);
            intersezione.retainAll(other.forseContenuti);
            return new CharacterInclusion(unione,intersezione);
        }else
            return bottom();
    }

    @Override
    protected CharacterInclusion wideningAux(CharacterInclusion other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(CharacterInclusion other) throws SemanticException {
        /*int Lothercont=other.Contenuti.length;
        int Lthisforsecont=this.forseContenuti.length;
        boolean contenuto=true;
        boolean forseContenuto=true;

        for(int i=0;i<Lothercont && contenuto;i++){
            //https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
            if(!Arrays.asList(this.Contenuti).contains(other.Contenuti[i]))
                contenuto=false;
        }
        for(int i=0;i<Lthisforsecont && forseContenuto;i++){
            if(!Arrays.asList(other.forseContenuti).contains(this.forseContenuti[i]))
                forseContenuto=false;
        }
         return contenuto && forseContenuto;*/
        return (this.Contenuti.containsAll(other.Contenuti) && other.forseContenuti.containsAll(this.forseContenuti));
    }

    @Override
    protected CharacterInclusion evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        /*if (constant.getValue() instanceof String) {
            String a = (String)constant.getValue();
            //pag 7(4.1.5)
            char[] b=a.toCharArray();
            String[] contenuto={};
            for(int i=0;i<b.length;i++){
                contenuto[i]=String.valueOf(b[i]);
            }
            return new CharacterInclusion(contenuto,contenuto);
        }
        return top();*/
        if(constant.getValue() instanceof String){
            String a = (String)constant.getValue();
            Set<Character> eval = new HashSet<>();
            char[] charArray=a.toCharArray();
            for(int i=0;i<a.length();i++){
                eval.add(charArray[i]);
            }
            return new CharacterInclusion(eval,eval);
        }else
            return top();
    }

    @Override
    protected CharacterInclusion evalBinaryExpression(BinaryOperator operator, CharacterInclusion left, CharacterInclusion right, ProgramPoint pp) throws SemanticException {
        /*if(operator instanceof AdditionOperator || operator instanceof AdditionOperator || operator instanceof Multiplication || operator instanceof DivisionOperator)
            return top();
        else if(operator instanceof StringConcat) {
            Set<String> unioneContenuti = new HashSet<>();
            unioneContenuti.addAll(Arrays.asList(left.Contenuti));
            unioneContenuti.addAll(Arrays.asList(right.Contenuti));
            String[] arrayUnioneContenuti={};
            arrayUnioneContenuti=unioneContenuti.toArray(arrayUnioneContenuti);
            Set<String> unioneForseContenuti = new HashSet<>();
            unioneForseContenuti.addAll(Arrays.asList(left.forseContenuti));
            unioneForseContenuti.addAll(Arrays.asList(right.forseContenuti));
            String[] arrayUnioneForseContenuti={};
            arrayUnioneForseContenuti=unioneContenuti.toArray(arrayUnioneForseContenuti);
            return new CharacterInclusion(arrayUnioneContenuti,arrayUnioneForseContenuti);
        } else  return top();*/
        if(operator instanceof StringConcat) {
            Set<Character> unionCertainlyContained = new HashSet<>();
            unionCertainlyContained.addAll(left.Contenuti);
            unionCertainlyContained.addAll(right.Contenuti);
            Set<Character> unionMaybeContained = new HashSet<>();
            unionMaybeContained.addAll(left.forseContenuti);
            unionMaybeContained.addAll(right.forseContenuti);
            return new CharacterInclusion(unionCertainlyContained, unionMaybeContained);
        }else
            return top();
    }
    private boolean controllo(String s, String[] ss){
        boolean trovato=false;
        for(int i = 0;i<ss.length;i++){
            if(ss[i]==s)
                trovato=true;
        }
        return trovato;
    }
   /*private boolean areChar(CharacterInclusion c){
        if(c.forseContenuti.length==1 && c.Contenuti.length==1 && c.forseContenuti[0]==c.Contenuti[0])
            return true;
        else
            return false;
    }*/

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, CharacterInclusion left, CharacterInclusion right, ProgramPoint pp) throws SemanticException {
        /*if(operator instanceof StringContains) {
            if(areChar(left)) {
                if (controllo(right.Contenuti[0], left.Contenuti))
                    return SATISFIED;
                else if (!controllo(right.Contenuti[0], left.forseContenuti))
                    return NOT_SATISFIED;
            }
        }
        return UNKNOWN;*/
        if(operator instanceof StringContains){
            if(left.Contenuti.containsAll(right.Contenuti))
                return SemanticDomain.Satisfiability.SATISFIED;
            else if(left.forseContenuti.containsAll(right.forseContenuti))
                return SemanticDomain.Satisfiability.NOT_SATISFIED;
        }
        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected CharacterInclusion evalTernaryExpression(TernaryOperator operator, CharacterInclusion left, CharacterInclusion middle, CharacterInclusion right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof StringSubstring) {
            return new CharacterInclusion(null, left.forseContenuti);
        }
        return top();
    }

    @Override
    public boolean equals(Object obj) {
        /*if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CharacterInclusion other = (CharacterInclusion) obj;
        return this.Contenuti == other.Contenuti &&
                this.forseContenuti==other.forseContenuti;*/
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CharacterInclusion other = (CharacterInclusion) obj;
        return Objects.equals(Contenuti, other.Contenuti) && Objects.equals(forseContenuti, other.forseContenuti);
    }

    @Override
    public int hashCode() {
        //return Objects.hash(Contenuti,forseContenuti);
        return Objects.hash(Contenuti, forseContenuti);
    }






}
