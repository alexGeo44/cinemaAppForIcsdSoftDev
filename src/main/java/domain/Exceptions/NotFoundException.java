package domain.Exceptions;

public class NotFoundException  extends DomainException{

    private final String resource;

    public NotFoundException(String resource , String message){

        super(message);
        this.resource = resource;

    }

    public String resource(){
        return resource;
    }

}
