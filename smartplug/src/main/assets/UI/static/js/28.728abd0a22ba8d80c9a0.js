webpackJsonp([28],{NhtP:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var o=a("Dd8w"),r=a.n(o),s=a("cNyQ"),n=a("NYxO"),l={data:function(){return{borderColor:!1,value:"",toastFalg:!1}},components:{"head-vue":s.a},computed:r()({},Object(n.c)(["getuserName"]),{firstName:function(){return this.getuserName}}),created:function(){this.value=this.firstName},mounted:function(){},methods:r()({},Object(n.b)(["userName"]),{nameExp:function(e){/^\w{0,}$/.test(e)?this.toastFalg=!1:this.toastFalg=!0},save:function(){""!==this.value&&(this.userName(this.value),this.$router.go(-1))},clear:function(){this.value=""}})},i={render:function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",{attrs:{id:"changename"}},[o("head-vue",{attrs:{title:"Change Name",goBack:"true"}},[o("div",{attrs:{slot:"setRight"},on:{click:e.save},slot:"setRight"},[e._v("Save")])]),e._v(" "),o("div",{staticClass:"editorName",class:e.borderColor?"active_color":""},[o("input",{directives:[{name:"model",rawName:"v-model",value:e.value,expression:"value"}],attrs:{type:"text",placeholder:"only letters and numbers allowed",maxlength:"16"},domProps:{value:e.value},on:{focus:function(t){e.borderColor=!e.borderColor},blur:function(t){e.borderColor=!e.borderColor},input:[function(t){t.target.composing||(e.value=t.target.value)},function(t){e.nameExp(e.value)}]}}),e._v(" "),o("img",{attrs:{src:a("vW/C"),alt:""},on:{click:e.clear}})]),e._v(" "),o("div",{staticClass:"text"},[e._v("1-16 characters")]),e._v(" "),e.toastFalg?o("div",{staticClass:"toast"},[e._v("\n    only letters and numbers allowed\n  ")]):e._e()],1)},staticRenderFns:[]};var u=a("VU/8")(l,i,!1,function(e){a("yNNi")},"data-v-0d0aa8fb",null);t.default=u.exports},yNNi:function(e,t){}});
//# sourceMappingURL=28.728abd0a22ba8d80c9a0.js.map