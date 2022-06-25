package br.com.fiap.scj.camelexample.services;

import br.com.fiap.scj.camelexample.beans.MyObject;

public class ObjectService {
    public static void execute(MyObject object){
        object.setName("Preenchendo meu object");
        object.setId(object.getId()*10);
    }
}
