(function(){var a={};zk.override(zk.Widget.prototype,a,{$binder:function(){var b=this;for(;b;b=b.parent){if(b["$ZKBINDER$"]){break}}if(b){if(!b._$binder){b._$binder=new zkbind.Binder(b,this)}return b._$binder}return null},$afterCommand:function(d,c){var b=this.$binder();if(b){b.$doAfterCommand(d,c)}},unbind_:function(){if(this._$binder){this._$binder.destroy();this._$binder=null}a.unbind_.apply(this,arguments)}});zkbind.$=function(d,b){var c=zk.Widget.$(d,b);if(c){return c.$binder()}zk.error("Not found ZK Binder with ["+d+"]")};zkbind.Binder=zk.$extends(zk.Object,{$init:function(b,c){this.$supers("$init",arguments);this.$view=b;this.$currentTarget=c;this._aftercmd={}},after:function(d,b){if(!b&&jq.isFunction(d)){b=d;d=this._lastcmd}var c=this._aftercmd[d];if(!c){this._aftercmd[d]=[b]}else{c.push(b)}return this},unAfter:function(e,c){var d=this._aftercmd[e];for(var b=d?d.length:0;b--;){if(d[b]==c){d.splice(b,1)}}return this},destroy:function(){this._aftercmd=null;this.$view=null;this.$currentTarget=null},command:function(c,b){this.$command0(c,b);return this},$command0:function(d,b,c){var e=this.$view;if(c){setTimeout(function(){zAu.send(new zk.Event(e,"onBindCommand",{cmd:d,args:b},{toServer:true}))},c)}else{zAu.send(new zk.Event(e,"onBindCommand",{cmd:d,args:b},{toServer:true}))}this._lastcmd=d},globalCommand:function(c,b){this.$globalCommand0(c,b);return this},$globalCommand0:function(d,b,c){var e=this.$view;if(c){setTimeout(function(){zAu.send(new zk.Event(e,"onBindGlobalCommand",{cmd:d,args:b},{toServer:true}))},c)}else{zAu.send(new zk.Event(e,"onBindGlobalCommand",{cmd:d,args:b},{toServer:true}))}this._lastcmd=d},$doAfterCommand:function(f,c){var e=this._aftercmd[f];for(var d=0,b=e?e.length:0;d<b;d++){e[d].apply(this,[c])}}},{postCommand:function(f,e,d){var c=zk.Widget.$(f);if(c){var b=c.$binder();if(b){b.command(e,d)}}},postGlobalCommand:function(f,e,d){var c=zk.Widget.$(f);if(c){var b=c.$binder();if(b){b.globalCommand(e,d)}}}})})();