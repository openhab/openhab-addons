(function(helper, $, undefined) {
  helper.initialize = function(jqueryId) {
    const $jqueryId = $(jqueryId);

    const koModel = new myModel();
    ko.applyBindings(koModel, $jqueryId[0]);
  };

  class myModel {
    constructor() {
      this.method = ko.observable(new currentMethod());
      this.result = ko.observable();
      this.fileTitle = ko.observable();
      this.selectedSortIdx = ko.observable();
      this.methods = ko.observableArray();
      this.sortedMethods = ko.pureComputed(() => {
        return this.methods.sorted(this.sort);
      });
    }

    loadFile(elm, event) {
      const files = event.target.files;
      if (files.length > 0) {
        const reader = new FileReader();

        reader.onload = e => {
          const jsonData = JSON.parse(reader.result);
          const defs = this.getMethods(jsonData);
          this.selectedSortIdx(-1);
          this.method(new currentMethod());
          this.fileTitle(jsonData.modelName);
          this.methods(defs);
          $.jGrowl(`Loaded ${defs.length} methods`);
        };
        reader.readAsText(files[0]);
      }
    }

    loadRestFile(elm, event) {
      const files = event.target.files;
      if (files.length > 0) {
        const reader = new FileReader();

        reader.onload = e => {
          const jsonData = JSON.parse(reader.result);
          const defs = this.getMethodsFromRestApi(jsonData);
          this.selectedSortIdx(-1);
          this.method(new currentMethod());
          this.fileTitle(jsonData.modelName);
          this.methods(defs);
          $.jGrowl(`Loaded ${defs.length} methods`);
        };
        reader.readAsText(files[0]);
      }
    }

    mergeFile(elm, event) {
      const files = event.target.files;
      if (files.length > 0) {
        const reader = new FileReader();

        reader.onload = e => {
          const jsonData = JSON.parse(reader.result);
          if (!this.fileTitle().includes(jsonData.modelName)) {
            const defs = this.getMethods(jsonData);
            this.selectedSortIdx(-1);
            this.method(new currentMethod());

            let idx = 0;
            const mthds = this.methods();
            defs.forEach(def => {
              if (!mthds.find(mthd => def.isSameDef(mthd))) {
                idx++;
                mthds.push(def);
              }
            });

            this.methods(mthds);

            if (this.fileTitle() === "") {
              this.fileTitle(jsonData.modelName);
            } else {
              this.fileTitle(this.fileTitle() + "," + jsonData.modelName);
            }
            $.jGrowl(`Loaded ${idx} methods`);
          } else {
            $.jGrowl(`Already loaded ${jsonData.modelName} methods`);
          }
        };
        reader.readAsText(files[0]);
      }
    }

    saveFile() {
      const services = new Map();
      this.methods().forEach(m => {
        const srv = services.get(m.serviceName);

        if (srv === undefined) {
          services.set(m.serviceName, new RestApi(m));
        } else {
          if (m.methodType === MethodDef.Method) {
            srv.methods.push(new RestApiMethod(m.method));
          } else {
            srv.notifications.push(new RestApiMethod(m.method));
          }
        }
      });

      const restApi = [...services.values()];
      const blb = new Blob([JSON.stringify(restApi)], {
        type: "application/json"
      });
      saveAs(blb, "restapi.json");
    }

    selectMethod(data, sortIdx) {
      this.selectedSortIdx(sortIdx);
      const idx = this.methods().indexOf(data);
      this.method(new currentMethod(this.methods()[idx]));
    }

    deleteMethod(idx) {
      this.method(this.method().splice(idx, 1));
    }

    runCommand() {
      this.result("waiting...");
      let parms = this.method().parms();
      if (Array.isArray(parms)) {
        parms = parms.join(",");
      }

      axios
        .post("/sony/app/execute", {
          baseUrl: this.method().baseUrl(),
          serviceName: this.method().service(),
          transport: this.method().transport(),
          command: this.method().command(),
          version: this.method().version(),
          parms
        })
        .then(
          res => {
            if (res.data.success === true) {
              this.result(res.data.results);
            } else {
              this.result(res.data.message);
            }
          },
          res => {
            const msg =
              res.response === undefined
                ? res.message
                : res.response.status + " " + res.response.statusText;
            $.jGrowl("Error executing: " + msg, {
              theme: "jgrowl-error",
              sticky: true
            });
            this.result(msg);
          }
        );
    }

    getMethods(jsonData) {
      const defs = [];
      jsonData.services.forEach(srv => {
        srv.methods.forEach(mthd =>
          defs.push(
            new MethodDef(
              jsonData.baseURL,
              jsonData.modelName,
              srv.serviceName,
              srv.version,
              srv.transport,
              new Method(
                mthd.baseUrl,
                mthd.service,
                mthd.transport,
                mthd.methodName,
                mthd.version,
                mthd.variation,
                mthd.parms,
                mthd.retVals
              ),
              MethodDef.Method
            )
          )
        );
        srv.notifications.forEach(mthd =>
          defs.push(
            new MethodDef(
              jsonData.baseURL,
              jsonData.modelName,
              srv.serviceName,
              srv.version,
              srv.transport,
              new Method(
                mthd.baseUrl,
                mthd.service,
                mthd.transport,
                mthd.methodName,
                mthd.version,
                mthd.variation,
                mthd.parms,
                mthd.retVals
              ),
              MethodDef.Notification
            )
          )
        );
      });
      return defs;
    }

    getMethodsFromRestApi(jsonData) {
      const defs = [];
      jsonData.forEach(srv => {
        srv.methods.forEach(m => {
          defs.push(
            new MethodDef(
              "",
              "",
              srv.serviceName,
              srv.version,
              "",
              new Method(
                "",
                srv.serviceName,
                "",
                m.methodName,
                m.version,
                m.variation,
                m.parms,
                m.retVals
              ),
              MethodDef.Method
            )
          );
        });
        srv.notifications.forEach(m => {
          defs.push(
            new MethodDef(
              "",
              "",
              srv.serviceName,
              srv.version,
              "",
              new Method(
                "",
                srv.serviceName,
                "",
                m.methodName,
                m.version,
                m.variation,
                m.parms,
                m.retVals
              ),
              MethodDef.Notification
            )
          );
        });
      });

      return defs;
    }

    sort(a, b) {
      if (a.serviceName < b.serviceName) {
        return -1;
      }
      if (a.serviceName > b.serviceName) {
        return 1;
      }
      if (a.methodType < b.methodType) {
        return -1;
      }
      if (a.methodType > b.methodType) {
        return 1;
      }
      if (a.method.command < b.method.command) {
        return -1;
      }
      if (a.method.command > b.method.command) {
        return 1;
      }
      const an = parseFloat(a.method.version);
      const bn = parseFloat(b.method.version);
      return an - bn;
    }
  }

  class currentMethod {
    constructor(mth) {
      this.baseUrl = ko.observable(mth === undefined ? "" : mth.baseUrl);
      this.transport = ko.observable(mth === undefined ? "" : mth.transport);
      this.service = ko.observable(mth === undefined ? "" : mth.serviceName);
      this.command = ko.observable(mth === undefined ? "" : mth.method.command);
      this.version = ko.observable(mth === undefined ? "" : mth.method.version);
      this.parms = ko.observable(
        mth === undefined || mth.method.parms === undefined
          ? ""
          : mth.method.parms.join(",")
      );
    }
  }

  class koResult {
    constructor() {
      this.result = ko.observable();
    }
  }

  class MethodDef {
    static Method = "M";
    static Notification = "N";

    constructor(
      baseUrl,
      modelName,
      serviceName,
      serviceVersion,
      transport,
      method,
      methodType
    ) {
      this.baseUrl = baseUrl;
      this.modelName = modelName;
      this.serviceName = serviceName;
      this.serviceVersion = serviceVersion;
      this.transport = transport;
      this.method = method;
      this.methodType = methodType;
    }

    isDuplicateKey(mth) {
      return (
        this.serviceName === mth.serviceName &&
        this.method.command === mth.method.command &&
        this.method.version === mth.method.version &&
        this.method.variation === mth.method.variation
      );
    }

    isSameDef(mth) {
      return (
        this.serviceName === mth.serviceName &&
        this.method.command === mth.method.command &&
        this.method.version === mth.method.version &&
        this.method.variation === mth.method.variation &&
        this.arrEquals(this.method.parms, mth.method.parms) &&
        this.arrEquals(this.method.retVals, mth.method.retVals) &&
        this.methodType === mth.methodType
      );
    }

    arrEquals(array1, array2) {
      const arr1 = array1.sort();
      const arr2 = array2.sort();
      return (
        arr1.length === arr2.length &&
        arr1.every((value, index) => value === arr2[index])
      );
    }
  }

  class Method {
    constructor(
      baseUrl,
      service,
      transport,
      command,
      version,
      variation,
      parms,
      retVals
    ) {
      this.baseUrl =
        baseUrl === undefined ? "http://192.168.1.167/sony" : baseUrl;
      this.service = service === undefined ? "service" : service;
      this.transport = transport === undefined ? "auto" : transport;
      this.command = command === undefined ? "getPowerStatus" : command;
      this.version = version === undefined ? "1.1" : version;
      this.variation = variation === undefined ? 0 : variation;

      const myParms =
        parms === undefined
          ? undefined
          : parms.filter(e => e !== undefined && e !== null && e !== "");
      this.parms = myParms === undefined ? [] : myParms;

      const myVals =
        retVals === undefined
          ? undefined
          : retVals.filter(e => e !== undefined && e !== null && e !== "");
      this.retVals = myVals === undefined ? [] : myVals;
    }
  }

  class RestApi {
    constructor(methodDef) {
      this.serviceName = methodDef.serviceName;
      this.version = methodDef.serviceVersion;
      this.methods = new Array();
      this.notifications = new Array();

      if (methodDef.method !== undefined) {
        if (methodDef.methodType === MethodDef.Method) {
          this.methods.push(new RestApiMethod(methodDef.method));
        } else {
          this.notifications.push(new RestApiMethod(methodDef.method));
        }
      }
    }
  }

  class RestApiMethod {
    constructor(mthd) {
      this.methodName = mthd.command;
      this.version = mthd.version;
      this.variation = mthd.variation;

      this.parms = mthd.parms;
      this.retVals = mthd.retVals;
    }
  }
})((window.sonyapp.capabilities = window.sonyapp.capabilities || {}), jQuery);
