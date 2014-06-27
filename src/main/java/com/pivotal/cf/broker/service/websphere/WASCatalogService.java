package com.pivotal.cf.broker.service.websphere;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.service.CatalogService;

/**
 * An implementation of the CatalogService that gets the catalog injected (ie configure 
 * in spring config)
 * 
 * @author sgreenberg@gopivotal.com
 *
 */
@Service
public class WASCatalogService implements CatalogService {

	private Catalog catalog;
	
	private Map<String,ServiceDefinition> serviceDefs = new HashMap<String,ServiceDefinition>();
	
	@Autowired
	public WASCatalogService(Catalog catalog) {
		this.catalog = catalog;
		initializeMap();
	}
	
	private void initializeMap() {
		for (ServiceDefinition def: catalog.getServiceDefinitions()) {
			serviceDefs.put(def.getId(), def);
		}
	}
	
	@Override
	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) {
		return serviceDefs.get(serviceId);
	}

}
