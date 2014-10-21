package org.openhab.io.transport.upnp;

import java.util.HashMap;
import java.util.Map;
import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ActionCallback;
import org.jupnp.controlpoint.SubscriptionCallback;
import org.jupnp.model.action.ActionArgumentValue;
import org.jupnp.model.action.ActionException;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.gena.CancelReason;
import org.jupnp.model.gena.GENASubscription;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.Service;
import org.jupnp.model.types.UDAServiceId;
import org.jupnp.model.types.UDN;
import org.jupnp.model.state.StateVariableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class UpnpIOServiceImpl implements UpnpIOService {

	private static final Logger logger = LoggerFactory.getLogger(UpnpIOServiceImpl.class);

	private UpnpService upnpService;

	private Map<UpnpIOParticipant,Device> participants = new HashMap<>();

	public class UpnpSubscriptionCallback extends SubscriptionCallback {

		public UpnpSubscriptionCallback(Service service) {
			super(service);
		}

		public UpnpSubscriptionCallback(Service service,
				int requestedDurationSeconds) {
			super(service, requestedDurationSeconds);
		}

		@Override
		protected void ended(GENASubscription subscription, CancelReason arg1,
				UpnpResponse arg2) {

			logger.debug("A GENA subscription '{}' for device '{}' was ended",subscription.getService().getServiceId(),subscription.getService().getDevice().getRoot().getIdentity().getUdn());

			Service service = subscription.getService();			
			UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(service,subscription.getActualDurationSeconds());
			upnpService.getControlPoint().execute(callback);			
		}

		@Override
		protected void established(GENASubscription arg0) {
			// TODO Auto-generated method stub
			logger.debug("A GENA subscription '{}' for device '{}' is established",arg0.getService().getServiceId().getId(),arg0.getService().getDevice().getRoot().getIdentity().getUdn());
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void eventReceived(GENASubscription sub) {

			Map<String, StateVariableValue> values = sub.getCurrentValues();  
			Device device = sub.getService().getDevice();

			logger.debug("Receiving a GENA subscription '{}' response for device '{}'",sub.getService().getServiceId().getId(),device.getRoot().getIdentity().getUdn());

			for(UpnpIOParticipant participant : participants.keySet()) {
				if(participants.get(participant).equals(device.getRoot())) {
					for(String stateVariable : values.keySet()){
						StateVariableValue value = values.get(stateVariable);
						if(value.getValue()!=null) {
							try {
								participant.onValueReceived(stateVariable, value.getValue().toString(),sub.getService().getServiceId().getId());
							}
							catch (Exception e) {
								logger.debug("Error {}",e);
							}
						}
					}
				} 					
				break;
			}
		}


		@Override
		protected void eventsMissed(GENASubscription arg0, int arg1) {
			// TODO Auto-generated method stub
			logger.debug("A GENA subscription '{}' for device '{}' missed events",arg0.getService().getServiceId(),arg0.getService().getDevice().getRoot().getIdentity().getUdn());

		}

		@Override
		protected void failed(GENASubscription arg0, UpnpResponse arg1,
				Exception arg2, String arg3) {
			// TODO Auto-generated method stub
			logger.debug("A GENA subscription '{}' for device '{}' failed",arg0.getService().getServiceId(),arg0.getService().getDevice().getRoot().getIdentity().getUdn());

		}

	}

	public void activate() {
		logger.debug("Starting UPnP IO service...");
	}

	public void deactivate() {
		logger.debug("Stopping UPnP IO service...");		
	}

	protected void setUpnpService(UpnpService upnpService) {
		this.upnpService = upnpService;
	}

	protected void unsetUpnpService(UpnpService upnpService) {
		this.upnpService = null;
	}

	public void addSubscription(UpnpIOParticipant participant, String serviceID, int duration) {

		if(participant!=null && serviceID != null ) {
			Device device = participants.get(participant);

			if(device==null) {
				device = upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true);
				if(device!=null) {
					logger.debug("Registering device '{}' for participant '{}'",device.getIdentity(),participant.getUDN());
					participants.put(participant, device);
				}
			}

			if(device!=null) {

				Device[] embedded = device.getEmbeddedDevices();
//				logger.debug("Device '{}' has {} embedded devices",device.getIdentity().getUdn(),embedded.length);
//				Service[] services = device.findServices();
//				for(Service aService : services) {
//					logger.debug("  Service '{}' on root device '{}' found",aService.getServiceId().getId(),device.getIdentity().getUdn().getIdentifierString());
//				}
//
//				for(Device aDevice : embedded) {
//					logger.debug("  Embedded device '{}' with UDN '{}' found", aDevice.getIdentity(),aDevice.getIdentity().getUdn().getIdentifierString());
//					Service[] subservices = aDevice.findServices();
//					for(Service aService : subservices) {
//						logger.debug("   Service '{}' on embedded device '{}' found",aService.getServiceId().getId(),aDevice.getIdentity().getUdn().getIdentifierString());
//					}
//				}

				Service subService = device.findService(new UDAServiceId(serviceID));
				Device theEmbeddedDevice = null;
				if(subService == null) {
					// service not on the root device, we search the embedded devices as well
					for(Device aDevice : embedded) {
						subService = aDevice.findService(new UDAServiceId(serviceID));
						if(subService!=null) {
							theEmbeddedDevice = aDevice;
							break;
						}
					}
				}

				if(subService != null) {
					logger.debug("Setting up an UPNP service subscription '{}' for particpant '{}'",serviceID, participant.getUDN());

					UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(subService,duration);
					upnpService.getControlPoint().execute(callback);
				} else {
					logger.debug("Could not find service '{}' for device '{}'",serviceID,device.getIdentity().getUdn());
				}
			} else {
				logger.debug("Could not find an upnp device for participant '{}'",participant.getUDN());

			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String,String> invokeAction(UpnpIOParticipant participant, String serviceID, String actionID, Map<String,String> inputs) {


		HashMap<String,String> resultMap = new HashMap<String,String>();

		if(serviceID!=null && actionID != null && participant != null) {

			Device device = participants.get(participant);

			if(device==null) {
				device = upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true);
				if(device!=null) {
					logger.debug("Registering device '{}' for participant '{}'",device.getIdentity(),participant.getUDN());
					participants.put(participant, device);
				}
			}

			if(device != null) {

				Service service = device.findService(new UDAServiceId(serviceID));
				if(service !=null) {

					
					Action action = service.getAction(actionID);
					if(action!=null) {
						
						ActionInvocation invocation = new ActionInvocation(action);	
						if(invocation!=null) {
							if(inputs!=null) {
								for(String variable : inputs.keySet()) {
									invocation.setInput(variable,inputs.get(variable));
								}
							}

							logger.debug("Invoking Action '{}' of service '{}' for participant '{}'", new Object[] {actionID,serviceID,participant.getUDN()});
							new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();

							ActionException anException = invocation.getFailure();
							if(anException!= null && anException.getMessage()!=null) {
								logger.warn(anException.getMessage());
							}

							Map<String, ActionArgumentValue> result =  invocation.getOutputMap();
							if(result != null) {
								for(String variable : result.keySet()) {
									ActionArgumentValue newArgument = null;
									try {
										newArgument = result.get(variable);
										if(newArgument.getValue() != null) {
											resultMap.put(variable,newArgument.getValue().toString());
										}
									}
									catch (Exception e) {
										logger.debug("An exception '{}' occurred processing ActionArgumentValue '{}' with value '{}'", new Object[]{e.getMessage(),newArgument.getArgument().getName(),newArgument.getValue()});
									}
								}
							}
						}
					} else {
						logger.debug("Could not find action '{}' for participant '{}'",actionID,participant.getUDN());
					}
				} else {
					logger.debug("Could not find service '{}' for participant '{}'",serviceID,participant.getUDN());
				} 
			} else {
				logger.debug("Could not find an upnp device for participant '{}'",participant.getUDN());
			} 
		}
		return resultMap;
	}

	@Override
	public boolean isRegistered(UpnpIOParticipant participant) {
		if(upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true)!=null) {
			return true;
		} else {
			return false;
		}
	}

}
