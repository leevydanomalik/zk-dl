<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

/*
    Document   : quickfilter
    Created on : 12.6.2013, 17:30:07
    Author     : Jan Tecl
    Description:
    Purpose of the stylesheet follows.
*/

.z-dlzklib-clear {
    clear: both;
}

/* Quickfilter component */
.z-quickfilter {
    font-size: 11px;
    float: left;
}
.z-quickfilter span {
    display: block;
}
.z-quickfilter-text {
    float: left;
    margin: 3px 4px 0 0;
}
.z-quickfilter-list {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/btn-open-list.png')}) no-repeat;
    float: left;
    height: 10px;
    width: 16px;
    margin: 9px 4px 0 0;
    cursor: pointer;
}
.z-quickfilter-real {
    background: #fff;
    border: 1px solid #E6E6E6;
    border-top-color: #B2B2B2;
    border-radius: 2px;
    -moz-border-radius: 2px;
    -webkit-border-radius: 2px;
    float: left;
    padding: 2px 20px 2px 5px;
}
.z-quickfilter-del {
    background: url(${c:encodeURL('~./js/dlzklib/img/btn-del-text.png')}) no-repeat;
    float: left;
    height: 16px;
    width: 16px;
    margin: 6px 0 0 -20px;
    position: static;
    cursor: pointer;
}
.z-quickfilter-magnifier {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/btn-magnifier.png')}) no-repeat;
    float: left;
    height: 16px;
    width: 16px;
    margin: 4px 0 0 3px;
    cursor: pointer;
}

/* DLPaging component */
.z-dlpaging {
    height: 35px;
}

.z-dlpaging-button-table {
    float: left;
    width: auto;
    margin-left: 8px;
}

.z-dlpaging-aux-content {
    text-align: center;
    vertical-align: middle;
    width: 80%;
    margin: 0 auto;
}


/* Listboxmanager component*/
.z-listboxmanager {
    float: right;
}

.z-listboxmanager td {
    padding: 0 3px;
}

.z-listboxmanager span { /* Images */
    display: block;
    height: 30px;
    width: 30px;
    /*margin: 4px 0 0 3px;*/
    cursor: pointer;
}

.z-listboxmanager-menu_items_small {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/menu_items_small.png')}) no-repeat;
}

.z-listboxmanager-sort_small {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/sort_small.png')}) no-repeat;
}

.z-listboxmanager-filter_small {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/filter_small.png')}) no-repeat;
}

.z-listboxmanager-filter_small_active {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/filter_small_active.png')}) no-repeat;
}

.z-listboxmanager-cancel_filter {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/cancel_filter.png')}) no-repeat;
}

.z-listboxmanager-excel_small {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/excel_small.png')}) no-repeat;
}

.z-listboxmanager-trash_small {
    background: url(${c:encodeThemeURL('~./js/dlzklib/img/trash.png')}) no-repeat;
}

/* Listcontrol component*/
.z-listcontrol {
    height: 35px;
}

.z-listcontrol-aux-content {
    float: left;
    text-align: center;
    vertical-align: middle;
    width: auto;
    margin: 0 auto 0 30px;
}


/* -- window: login,error,notFound -- */
/*
    Document   : _login
    Created on : 18.6.2013, 13:02:19
    Author     : UZIVATEL
    Description:
        Purpose of the stylesheet follows.
*/

/* -- window: login,error,notFound -- */
.z-login { background-image:none; background-color:#545E6B; font-size: 12px; margin: 0 auto; }
.z-login .z-login-header { color: white; font-weight: bold; padding: 7px 5px;  }

.z-login .z-login-lbl { display: block; margin-bottom: 3px; }
.z-login input { border: 1px solid #717171; font-size: 15px; outline: none; padding: 5px 30px 5px 5px; }
.z-login .z-image { position: relative; top: -22px; left: 176px; }

.z-login .z-login-cm { border: 1px solid #545E6B; padding: 20px 0 30px 0;  }
.z-login .z-login-cnt { margin: 0 auto; position: relative; width: 200px; }

.z-login .z-login-rememberme { display: block; margin-top: -10px; }
.z-login .z-login-rememberme input[type=checkbox] { margin-right: 5px; position: relative; top: 1px; }
.z-login .z-login-submit { border: 1px solid #ccc; border-bottom: 1px solid #b4b4b4; color: #666; margin-top: 15px; padding: 4px 0; text-shadow: 0 1px 1px #fff; width: 198px; }

.z-login .z-login-error { color: red; margin-bottom: 15px; position: relative; left: -18px; width: 300px; }

/* -- CSS3 -- */
.z-login input,
.z-login input:focus,
.z-login .z-login-cm,
.z-login .z-login-submit,
.z-login .z-login-submit:hover { behavior: url('css/PIE/PIE.htc'); position: relative; }

.z-login input {
    background: -moz-linear-gradient(top, rgba(201,201,202,1) 0%, rgba(255,255,255,0.79) 70%, rgba(255,255,255,1) 100%);
    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(201,201,202,1)), color-stop(70%,rgba(255,255,255,0.79)), color-stop(100%,rgba(255,255,255,1)));
    background: -webkit-linear-gradient(top, rgba(201,201,202,1) 0%,rgba(255,255,255,0.79) 70%,rgba(255,255,255,1) 100%);
    background: -o-linear-gradient(top, rgba(201,201,202,1) 0%,rgba(255,255,255,0.79) 70%,rgba(255,255,255,1) 100%);
    background: -ms-linear-gradient(top, rgba(201,201,202,1) 0%,rgba(255,255,255,0.79) 70%,rgba(255,255,255,1) 100%);
    background: linear-gradient(top, rgba(201,201,202,1) 0%,rgba(255,255,255,0.79) 70%,rgba(255,255,255,1) 100%);
    -pie-background: linear-gradient(top, rgba(201,201,202,1) 0%,rgba(255,255,255,0.79) 70%,rgba(255,255,255,1) 100%);
}
.z-login input:focus {
    -moz-box-shadow: 0px 0px 5px #007eff;
    -webkit-box-shadow: 0px 0px 5px #007eff;
    box-shadow: 0px 0px 5px #007eff;
}
.z-login input.error {
    -moz-box-shadow: 0px 0px 5px red;
    -webkit-box-shadow: 0px 0px 5px red;
    box-shadow: 0px 0px 5px red;
}
.z-login input.error:focus {
    -moz-box-shadow: 0px 0px 5px #007eff;
    -webkit-box-shadow: 0px 0px 5px #007eff;
    box-shadow: 0px 0px 5px #007eff;
}
.z-login .z-login-cm {
    background: #e2e2e2;
    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #e2e2e2), color-stop(100%, #fafafa));
    background: -webkit-linear-gradient(top, #e2e2e2, #fafafa);
    background: -moz-linear-gradient(top, #e2e2e2, #fafafa);
    background: -ms-linear-gradient(top, #e2e2e2, #fafafa);
    background: -o-linear-gradient(top, #e2e2e2, #fafafa);
    background: linear-gradient(top, #e2e2e2, #fafafa);
    -pie-background: linear-gradient(top, #e2e2e2, #fafafa);
}
.z-login .z-login-submit {
    background: #f3f3f3;
    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #f3f3f3), color-stop(50%, #dddddd), color-stop(50%, #d2d2d2), color-stop(100%, #dfdfdf));
    background: -webkit-linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    background: -moz-linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    background: -ms-linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    background: -o-linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    background: linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    -pie-background: linear-gradient(top, #f3f3f3 0%, #dddddd 50%, #d2d2d2 50%, #dfdfdf 100%);
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
    border-radius: 5px;
}
.z-login .z-login-submit:hover {
    background: #e5e5e5;
    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #e5e5e5), color-stop(50%, #d1d1d1), color-stop(50%, #c4c4c4), color-stop(100%, #b8b8b8));
    background: -webkit-linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
    background: -moz-linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
    background: -ms-linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
    background: -o-linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
    background: linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
    -pie-background: linear-gradient(top, #e5e5e5 0%, #d1d1d1 50%, #c4c4c4 50%, #b8b8b8 100%);
}
.z-login .z-login-submit:active {
    -moz-box-shadow: inset 0 0 30px 0 #999999, 0 1px 0 0 white;
    -webkit-box-shadow: inset 0 0 30px 0 #999999, 0 1px 0 0 white;
    box-shadow: inset 0 0 30px 0 #999999, 0 1px 0 0 white;
}