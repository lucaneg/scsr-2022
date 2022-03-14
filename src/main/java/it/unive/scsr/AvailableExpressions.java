package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
public class AvailableExpressions
        implements DataflowElement<
        DefiniteForwardDataflowDomain<AvailableExpressions>,
        AvailableExpressions>{

    private final ValueExpression espressione;
    private final Identifier id;
    private final CodeLocation point;


    public AvailableExpressions() {
        this(null, null, null);
    }

    public AvailableExpressions(ValueExpression espressione, Identifier id, CodeLocation point) {
        this.espressione = espressione;
        this.id = id;
        this.point = point;
        /*
        this.id = id;
        this.point = point;
        this.espressione = espressione;
        */
    }

    @Override
    public int hashCode() {
        return Objects.hash(espressione, id, point);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AvailableExpressions other = (AvailableExpressions) obj;
        return Objects.equals(id, other.id) && Objects.equals(espressione, other.espressione) && Objects.equals(point, other.point);
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        result.add(id);
        return result;
    }
    //GEN
    @Override
    public Collection<AvailableExpressions> gen(Identifier id,
                                                ValueExpression espressione,
                                                ProgramPoint pp,
                                                DefiniteForwardDataflowDomain<AvailableExpressions> domain)
            throws SemanticException {

        Set<AvailableExpressions> result = new HashSet<>();
        AvailableExpressions ae;
        ae = new AvailableExpressions( espressione, id ,pp.getLocation());
        result.add(ae);
        return result;
    }
    //GEN SENZA ESRESSIONE
    @Override
    public Collection<AvailableExpressions> gen(ValueExpression expression,
                                                ProgramPoint pp,
                                                DefiniteForwardDataflowDomain<AvailableExpressions> domain)
            throws SemanticException {
        return new HashSet<>();
    }
    //CONVERTE ID/ESPRESSIONE/OBJ A STRING, CREATI PER PROBLEMI CON .toString()
    private String idtoString(Identifier identificatore){
        return identificatore+"";
    }
    private String exptoString(ValueExpression espressione){
        return espressione+"";
    }
    private String objtoString(Object oggetto){
        return oggetto+"";
    }

    private boolean contienecheckid(String espressione,
                                    Identifier identificatore) {
       /*    if(espressione.contains(objtoString(oggetto)))
                return true;
             else
                return false;*/

        if(espressione.contains(idtoString(identificatore)))
            return true;
        else
            return false;
    }

    private boolean contienecheckobj(String espressione,
                                     Object oggetto) {
        if(espressione.contains(objtoString(oggetto)))
            return true;
        else
            return false;
    }


    @Override
    public Collection<AvailableExpressions> kill(Identifier id,
                                                 ValueExpression expression,
                                                 ProgramPoint pp,
                                                 DefiniteForwardDataflowDomain<AvailableExpressions> domain)
            throws SemanticException {

        Set<AvailableExpressions> result = new HashSet<>();

        for (AvailableExpressions ae : domain.getDataflowElements()) {
            if (contienecheckid(exptoString(ae.espressione), id)) {
                result.add(ae);
            }
        }
        return result;
    }


    //KILL SENZA ESPRESSIONE
    @Override
    public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
                                                 DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {
        return new HashSet<>();
    }


    //NON TOCCO
    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(espressione);
    }

    @Override
    public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
        return new AvailableExpressions((ValueExpression) espressione.pushScope(scope), id, point);
    }

    @Override
    public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
        return new AvailableExpressions((ValueExpression) espressione.popScope(scope), id, point);
    }
}

