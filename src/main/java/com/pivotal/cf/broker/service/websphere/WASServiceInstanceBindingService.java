/**
 * 
 */
package com.pivotal.cf.broker.service.websphere;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
//import com.pivotal.cf.broker.repos.OracleDBServiceBindingRepository;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
import com.pivotal.cf.broker.util.WASManager;

/**
 * @author opstack
 * 
 */
@Service
public class WASServiceInstanceBindingService implements
		ServiceInstanceBindingService {

	private WASManager dbManager;
	private Map<String, ServiceInstanceBinding> bindingRepo;

	@Autowired
	public WASServiceInstanceBindingService(WASManager dbManager) {
		this.dbManager = dbManager;
		bindingRepo = new HashMap<String, ServiceInstanceBinding>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pivotal.cf.broker.service.ServiceInstanceBindingService#
	 * createServiceInstanceBinding(java.lang.String,
	 * com.pivotal.cf.broker.model.ServiceInstance, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			String bindingId, ServiceInstance serviceInstance,
			String serviceId, String planId, String appGuid)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {
		// TODO Auto-generated method stub
		ServiceInstanceBinding binding = bindingRepo.get(bindingId);
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}

		String database = serviceInstance.getId();
		String username = appGuid;
		// TODO Password Generator
		String password = "password";

		// TODO check if user already exists in the DB
		Map<String, String> connArr = dbManager.createUser(username, password,
				database);
		if (connArr.isEmpty())
			throw new ServiceBrokerException(
					"Failed to create new service binding: " + bindingId);
		// mongo.createUser(database, username, password);
		String uri = connArr.get("driver") + ":@" + connArr.get("host") + ":"
				+ connArr.get("port") + "/" + database;
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri", uri);
		credentials.put("driver", connArr.get("driver"));
		credentials.put("host", connArr.get("host"));
		credentials.put("port", connArr.get("port"));
		credentials.put("database", database);
		credentials.put("username", username);
		credentials.put("password", password);

		binding = new ServiceInstanceBinding(bindingId,
				serviceInstance.getId(), credentials, null, appGuid);
		// repository.save(binding);
		bindingRepo.put(binding.getId(), binding);

		return binding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pivotal.cf.broker.service.ServiceInstanceBindingService#
	 * getServiceInstanceBinding(java.lang.String)
	 */
	@Override
	public ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return bindingRepo.get(id);
		// return bindingRepo.findOne(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pivotal.cf.broker.service.ServiceInstanceBindingService#
	 * deleteServiceInstanceBinding(java.lang.String)
	 */
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(String id)
			throws ServiceBrokerException {
		/*
		 * ServiceInstanceBinding serviceBinding = bindingRepo.findOne(id); if
		 * (serviceBinding != null) {
		 * //mongoDBAdminService.deleteUser(serviceInstanceBinding
		 * .getServiceInstanceId(), id); bindingRepo.delete(id); }
		 */
		ServiceInstanceBinding serviceBinding = bindingRepo.remove(id);
		String username = serviceBinding.getAppGuid();
		if(username == null)
			throw new ServiceBrokerException("User could not be found: " + id);
		String result = dbManager.deleteUser(username);
		if(result == null)
			throw new ServiceBrokerException("Binding could not be deleted: " + id);
		return serviceBinding;
	}

}
