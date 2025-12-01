package domain.Exceptions;

public class StateTransitionForbidden extends DomainException {

    public StateTransitionForbidden(String message){
        super(message);
    }

}
