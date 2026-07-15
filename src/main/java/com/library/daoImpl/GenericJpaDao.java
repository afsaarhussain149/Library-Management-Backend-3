package com.library.daoImpl;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.library.dao.IGenericDao;

@Repository
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class GenericJpaDao extends AbstractJpaDao implements IGenericDao{

   
}
