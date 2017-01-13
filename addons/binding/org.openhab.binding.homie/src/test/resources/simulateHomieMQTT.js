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

HomieDevice.prototype.sendprops=function(){
	//Nodes and props
	mqttspy.publish(basetopic+this.id+"/temp/$type","temperature",1,true);
	mqttspy.publish(basetopic+this.id+"/temp/$properties","unit,degrees,sensorinterval:settable",1,true);
	mqttspy.publish(basetopic+this.id+"/temp/unit","c",1,true);
	mqttspy.publish(basetopic+this.id+"/temp/degrees","15.6",1,true);
	mqttspy.publish(basetopic+this.id+"/temp/sensorinterval","1",1,true);
}


function publish() {
	//var Thread = Java.type("java.lang.Thread");
	for(i=0;i<3;i++){
		var device=new HomieDevice("abc123-"+i,"D1 Mini "+i,"MyFirmware");
		device.announce();
		if(i==1){
			device.sendprops();
		}
	}

}

publish();
