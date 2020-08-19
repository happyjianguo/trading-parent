    /***************************************************************************
	 * 
	 * @Date 2019-11-06 17:30:00
	 * @author jiangchengyong
	 * 
	 **************************************************************************/


  var buyerNameQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.code + ' | ' + dataItem.name + ' | ' + dataItem.contactsPhone
                            }
                        );
                    })
                }
            }else{
                // bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_buyer_name').val(suggestion.name);
        }
    };

  var sellerNameQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.code + ' | ' + dataItem.name + ' | ' + dataItem.contactsPhone
                            }
                        );
                    })
                }
            }else{
                // bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_seller_name').val(suggestion.name);
        }
    };




    
    var buyerCardQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.customerName +"|"+dataItem.cardNo
                            }
                        );
                    })
                }
            }else{
                // bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_buyer_name_by_card_no').val(suggestion.customerName);
            $('#buyerCardNo').val(suggestion.cardNo);
        }
    };
    var sellerCardQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.customerName +"|"+dataItem.cardNo
                            }
                        );
                    })
                }
            }else{
                // bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_seller_name_by_card_no').val(suggestion.customerName);
            $('#sellerCardNo').val(suggestion.cardNo);
        }
    };

    function swipeCard(el,val1){
    	let cardNo=callbackObj.readCardNumber();
    	if (cardNo!=-1) {
	    	$(el).prevAll()[3].value=cardNo;
	    	$(val1).focus();
    	}
    }

    function queryCustomerByCardNo(cardNo, domId) {
        $('#' + domId).val('');
        if (!cardNo || $.trim(cardNo).length !== 12) {
            return;
        }
        $.ajax({
            type:'GET',
            url:'/customer/infoByCardNo.action?cardNo=' + cardNo,
            dataType:'json',
            success:function(result) {
                if (result.success) {
                    $('#' + domId).val(result.data.name);
                } else {

                }
            },
            error:function(){

            }
        });
    }
    
    
    // 结算员名称
    var operatorNameAutoCompleteOption = {
        serviceUrl: '/weighingBill/listOperatorByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'realName',
        showNoSuggestionNotice: true,
        noSuggestionNotice: '操作员不存在',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.serialNumber + '|' + dataItem.realName
                            }
                        );
                    })
                }
            }else{
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#certificateNumber, #_certificateNumber').val(suggestion.certificateNumber);
            $('#customerCellphone').val(suggestion.contactsPhone);
            $('#certificateNumber, #_certificateNumber, #customerCellphone').valid();
        }
    };

    // 时间范围
    lay('.laydatetime').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            , type: 'datetime'
            , min: getLastYearYestdy(new Date())
            , max: timeStamp2String(new Date().getTime())
        });
    });

    function getLastYearYestdy(date) {
    var strYear = date.getFullYear() - 1;
    var strDay = date.getDate();
    var strMonth = date.getMonth() + 1;
    if (strMonth < 10) {
    strMonth = "0" + strMonth;
    }
    if (strDay < 10) {
    strDay = "0" + strDay;
    }
    return strYear + "-" + strMonth + "-" + strDay;
    }

    function timeStamp2String(time) {
    var datetime = new Date();
    datetime.setTime(time);
    var year = datetime.getFullYear();
    var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
    var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
    return year + "-" + month + "-" + date;
    }

    /** *******************变量定义区 begin************ */
        // 行索引计数器
        // 如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    let _modal = $('#_modal');

    /** *******************变量定义区 end************** */


    /** ****************************驱动执行区 begin************************** */
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {url: '/weighingBill/listPage.action', pageSize: parseInt(size)});
    });

    /** ****************************驱动执行区 end*************************** */

    /**
	 * ***************************************函数区
	 * begin***********************************
	 */
    /**
	 * 打开新增窗口
	 */
    function invalidate() {
    	let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_state!=1) {
        	bs4pop.alert('该单据当前状态不能进行作废操作！');
        	return;
        }
    	bs4pop.confirm(" 确定作废当前单据吗？", {title: "确认提示"}, function (sure) {
            if (sure) {
		        $('#_modal .modal-body').load("/weighingBill/operatorInvalidate.html?id="+rows[0].id);
		        _modal.find('.modal-title').text('作废校验');
            	$("#_modal").modal();
            }
        });
    }
    
    function invalidateHandler(){
                    $.ajax({
                    type: "POST",
                    dataType: "json",
                    url:'/weighingBill/operatorInvalidate.action',
                    data: $('#validatePasswordForm').serialize(),
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            return;
                        }
                        window.location.reload();
                    },
                    error: function (data) {
                    	console.log(data);
                        bui.loading.hide();
                        bs4pop.alert("请求失败!", {type: 'error'});
                    }
                });
    }
    
     function withdraw() {
    	let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_state!=4) {
        	bs4pop.alert('该单据当前状态不能进行撤销操作！');
        	return;
        }
    	bs4pop.confirm(" 确定撤销当前单据吗？", {title: "确认提示"}, function (sure) {
            if (sure) {
		        $('#_modal .modal-body').load("/weighingBill/operatorWithdraw.html?id="+rows[0].id);
		        _modal.find('.modal-title').text('作废校验');
            	$("#_modal").modal();
            }
        });
    }
    
    function withdrawHandler(){
                    $.ajax({
                    type: "POST",
                    dataType: "json",
                    url:'/weighingBill/operatorWithdraw.action',
                    data: $('#validatePasswordForm').serialize(),
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            return;
                        }
                        window.location.reload();
                    },
                    error: function (data) {
                    	debugger;
                    	console.log(data);
                        bui.loading.hide();
                        bs4pop.alert("请求失败!", {type: 'error'});
                    }
                });
    }

    /**
	 * 打开修改窗口
	 */
    function openUpdateHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        $("#_modal").modal("show");

        $('#_modal .modal-body').load("/truck/edit.html?id=" + rows[0].id);
        _modal.find('.modal-title').text('修改注册车辆');
    }
    /**
	 * 打开查看窗口
	 */
    function openViewHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        $("#_modal").modal("show");

        $('#_modal .modal-body').load("/truck/view.html?id=" + rows[0].id);
        _modal.find('.modal-title').text('查看图片');
    }

    function openDetailHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        window.location.href='${contextPath}/weighingBill/detail.html?id='+rows[0].id;
    }

    /**
	 * 保存及更新表单数据
	 */
    function saveOrUpdateHandler() {
        var form = $("#_form");
        if (form.validate().form() != true) {
            return false;
        }

        bui.loading.show('努力提交中，请稍候。。。');
        let _formData = bui.util.removeKeyStartWith(form.serializeObject(), "_");
        let _url = null;
        // 没有id就新增
        if (_formData.id == null || _formData.id == "") {
            _url = "${contextPath}/booth/insert";
        } else {// 有id就修改
            _url = "${contextPath}/booth/update.action";
        }
        $.ajax({
            type: "POST",
            url: _url,
            data: _formData,
            processData: true,
            dataType: "json",
            async: true,
            success: function (data) {
                bui.loading.hide();
                if (data.code == "200") {
                    _grid.bootstrapTable('refresh');
                    _modal.modal('hide');
                } else {
                    bs4pop.alert(data.result, {type: 'error'});
                }
            },
            error: function (a, b, c) {
                bui.loading.hide();
                bs4pop.alert('远程访问失败', {type: 'error'});
            }
        });
    }


    /**
	 * 查询处理
	 */
    function queryDataHandler() {
        _grid.bootstrapTable('refresh');
    }

    /**
	 * table参数组装 可修改queryParams向服务器发送其余的参数
	 * 
	 * @param params
	 */
    function queryParams(params) {
        let temp = {
            rows: params.limit,   // 页面大小
            page: ((params.offset / params.limit) + 1) || 1, // 页码
            sort: params.sort,
            order: params.order
        };
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }

    /**
	 * ***************************************函数区
	 * end*************************************
	 */

    /**
	 * ***************************************自定义事件区
	 * begin***********************************
	 */


    function updateStatus(status, queryBtn){
        var inputval = $("input[name=status]").val();
        if(status == inputval){
            return;
        }else{
            $(".queryBtn .queryBtnCss").removeClass("queryBtnCss");
            $(queryBtn).addClass("queryBtnCss");
        }
        $("input[name=status]").val(status);
        queryDataHandler();
    }
    $(function(){
        $('#grid').on('click-row.bs.table', function (e, row, $element, field) {
            if(row.$_status == 1){
                $("#btn_enabled").attr("disabled", "disabled");
                $("#btn_disabled").attr("disabled", false);
                $("#btn_enabled").addClass("btn_css_disabled");
                $("#btn_disabled").removeClass("btn_css_disabled");
            }else if(row.$_status == 2){
                $("#btn_disabled").attr("disabled", "disabled");
                $("#btn_enabled").removeClass("btn_css_disabled");
                $("#btn_disabled").addClass("btn_css_disabled");
                $("#btn_enabled").attr("disabled", false);
            }else{
                $("#btn_disabled").attr("disabled", "disabled");
                $("#btn_enabled").attr("disabled", "disabled");
                $("#btn_disabled").addClass("btn_css_disabled");
                $("#btn_enabled").addClass("btn_css_disabled");
            }
        })
    })
    /**
	 * ***************************************自定义事件区
	 * end*************************************
	 */