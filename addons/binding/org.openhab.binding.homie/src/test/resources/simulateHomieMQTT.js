var basetopic="homie/";


function HomieDevice(deviceId,deviceName,fwname){
	this.id=deviceId;
	this.refreshinterval=5; //in seconds
	this.deviceName=deviceName;
	this.fwname=fwname;
	this.fwversion="v1";
}

HomieDevice.prototype.announce=function(){
	mqttspy.publish(basetopic+this.id+"/$homie","v2",1,true);
	mqttspy.publish(basetopic+this.id+"/$online","true",1,true);
	mqttspy.publish(basetopic+this.id+"/$name",this.deviceName,1,true);
	mqttspy.publish(basetopic+this.id+"/$localip","127.0.0.1",1,true);
	mqttspy.publish(basetopic+this.id+"/$mac","ab:cd:ef:12:34:56",1,true);
	mqttspy.publish(basetopic+this.id+"/$stats/uptime","0",1,true);
	mqttspy.publish(basetopic+this.id+"/$stats/signal","80",1,true);
	mqttspy.publish(basetopic+this.id+"/$stats/interval",this.refreshinterval,1,true);
	mqttspy.publish(basetopic+this.id+"/$fw/name",this.fwname,1,true);
	mqttspy.publish(basetopic+this.id+"/$fw/version",this.fwversion,1,true);
	mqttspy.publish(basetopic+this.id+"/$fw/checksum","79af87723dc295f95bdb277a61189a2a",1,false);
	mqttspy.publish(basetopic+this.id+"/$implementation","D1Mini",1,true);
	mqttspy.publish(basetopic+this.id+"/$implementation","D1Mini",1,true);
}



function ESHHomieDevice(deviceId,deviceName,fwname){
	this.id=deviceId;
	this.refreshinterval=5; //in seconds
	this.deviceName=deviceName;
	this.fwname=fwname;
	this.fwversion="v1";
}
ESHHomieDevice.prototype=Object.create(HomieDevice.prototype);

ESHHomieDevice.prototype.sendprops=function(){
	//Nodes and props
	mqttspy.publish(basetopic+this.id+"/heater/$type","ESH:Temperature",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/$properties","value:settable,unit,itemtype,min,max,step",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/unit","°C",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/itemtype","Number",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/value","25.4",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/min","20.0",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/max","30.0",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/step","0.5",1,true);
	mqttspy.publish(basetopic+this.id+"/heater/desc","Heater in livingroom",1,true);
}


function publish() {
	//var Thread = Java.type("java.lang.Thread");
	for(i=0;i<2;i++){
		var device=new ESHHomieDevice("abc123-"+i,"ESH D1 Mini "+i,"MyFirmware");
		device.announce();
		if(i==1){
			device.sendprops();
		}
	}


}

publish();
