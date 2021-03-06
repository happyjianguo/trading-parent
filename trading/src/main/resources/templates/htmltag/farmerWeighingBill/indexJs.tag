<script>
    /***************************************************************************
	 * 
	 * @Date 2019-11-06 17:30:00
	 * @author jiangchengyong
	 * 
	 **************************************************************************/

function clearQueryForm(){
    $('#queryForm input').val('');
    $('#statementStates').val(null).trigger('change');
    $('#tradeType').val(null).trigger('change');
    $('#goodsIds').val(null).trigger('change');
}

function doPrintHandler(){
    
    var visibleColumns= $('#grid').bootstrapTable('getVisibleColumns');
    
    $.ajax({
            type: "POST",
            url: "/farmerTradingBill/listPage.action",
            data: JSON.stringify(queryParams({
            	exportData:true,
                limit: 99999,   // 页面大小
                page: 1 // 页码
            })),
            dataType: "json",
            processData: false,
            contentType: "application/json",
            async: true,
            success: function (res) {
                var printObj={
                    data:[]
                };
                $(res.rows).each(function(index,item){
                    for(var key in item){
                        if (item[key] instanceof Object) {
                            for(var k in item[key]){
                                if(typeof item[key+'.'+k] !== "undefined" && item[key+'.'+k] !== null){
                                    continue;
                                }
                                var flag=false;
                                for(var i=0;i<visibleColumns.length;i++){
                                    if(visibleColumns[i].field==(key+'.'+k)){
                                       flag=true;
                                       break;
                                    }
                                }
                                if (!flag) {
                                    continue;
                                }
                                printObj.data.push({
                                    rowIndex:index,
                                    column:displayMap[key+'.'+k],
                                    value:item[key][k]
                                });
                            }
                        }else{
                            var flag=false;
                            for(var i=0;i<visibleColumns.length;i++){
                                if(visibleColumns[i].field==key){
                                   flag=true;
                                   break;
                                }
                            }
                            if (!flag) {
                                continue;
                            }
                            printObj.data.push({
                                    rowIndex:index,
                                    column:displayMap[key],
                                    value:item[key]
                                });                            
                        }
                        
                    }
                });
                var createdStart=$('#operationStartTime').val();
                var createdEnd=$('#operationEndTime').val();
                
                if (createdStart && createdEnd) {
                    createdStart=new Date(createdStart);
                    createdEnd=new Date(createdEnd);
                }
                
                if (!createdStart && !createdEnd) {
                    createdStart= new Date();
                    createdStart.setHours(0);
                    createdStart.setMinutes(0);
                    createdStart.setSeconds(0);
                    createdEnd= new Date();
                }
                
                if (createdStart && !createdEnd) {
                    createdStart=new Date(createdStart);
                    createdEnd=new Date(createdStart);
                    createdEnd.setDate(createdEnd.getDate()+366);
                    createdEnd.setHours(23);
                    createdEnd.setMinutes(59);
                    createdEnd.setSeconds(59);
                }
        
                if (!createdStart && createdEnd) {
                    createdEnd=new Date(createdEnd);
                    createdStart=new Date(createdEnd);
                    createdStart.setDate(createdStart.getDate()-366);
                    createdStart.setHours(0);
                    createdStart.setMinutes(0);
                    createdStart.setSeconds(0);
                }
                if (daysDistance(createdStart,createdEnd) > 367|| daysDistance(new Date(),createdEnd)>0) {
                    createdEnd=new Date();
                }
                
                createdStart= moment(createdStart).format("YYYY-MM-DD HH:mm:ss");
                createdEnd= moment(createdEnd).format("YYYY-MM-DD HH:mm:ss");
                printObj.startDate=createdStart;
                printObj.endDate=createdEnd;
                console.log(printObj);
                callbackObj.printPreview(JSON.stringify(printObj),"1","SettlementListDocument",0);
            },
            error: function (error) {
                bs4pop.alert(error.message, {type: 'error'});
            }
        });
    
}


// date1和date2是2019-06-18格式
function daysDistance(startDate, endDate) {     
    if (startDate>endDate){
        return 0;
    }
    if (startDate==endDate){
        return 1;
    }
    var days=(endDate - startDate)/(1*24*60*60*1000);
    return  days;
}

  var buyerNameQueryAutoCompleteOption = {
        serviceUrl: '/farmerTradingBill/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        width: '320px',
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


    function doReprintHandler() {
    // 获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        var url='';
        var serialNo=null;
        var id=null;
        if (rows[0].statement.state==4) {
            url='/farmerTradingBill/getWeighingBillPrintData.action'
            id=rows[0].id;
        }else if (rows[0].statement.state==2) {
            url='/farmerTradingBill/getWeighingStatementPrintData.action'
            serialNo= rows[0].statement.serialNo;
        }else{
            bs4pop.alert("当前单据状态不能补打单据!", {type: 'error'});
            return;
        }
        // 先拿到票据信息，在调用c端打印
        $.ajax({
            type: "POST",
            dataType: "json",
            url: url,
            data: {id:id,serialNo: serialNo,reprint:true},
            success: function (data) {
                bui.loading.hide();
                if (data.code == '200') {
                    // 调用c端打印
                    if (rows[0].statement.state==4) {
// callbackObj.printDirect(JSON.stringify(data.data),"WeighingDocument");
                        callbackObj.printPreview(JSON.stringify(data.data),"1","WeighingDocument",0);
                    }else if (rows[0].statement.state==2) {
                        // 冻结单打印过磅单数据
                        if(rows[0].measureType==1){
// callbackObj.printDirect(JSON.stringify(data.data),"SettlementDocument");
                            callbackObj.printPreview(JSON.stringify(data.data),"1","SettlementDocument",0);
                        }else{
// callbackObj.printDirect(JSON.stringify(data.data),"SettlementPieceDocument");
                            callbackObj.printPreview(JSON.stringify(data.data),"1","SettlementPieceDocument",0);
                        }
                    }
                }           
            },
            error: function () {
            bui.loading.hide();
            bs4pop.alert("打印失败!", {type: 'error'});
            }
        });


    }

  var sellerNameQueryAutoCompleteOption = {
        serviceUrl: '/farmerTradingBill/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        width: '320px',
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
        serviceUrl: '/farmerTradingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: '320px',
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
        serviceUrl: '/farmerTradingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: '320px',
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

    function swipeCard(){
        let cardNum;
        let json = JSON.parse(callbackObj.readCardNumber());
         if (json.code == 0) {
            cardNum = json.data;
        } else {
            bs4pop.alert(json.message, {type: "error"});
            return false;
        }
            if (cardNum!=-1) {
                $.ajax({
                    type:'GET',
                    url:'${contextPath!}/farmerTradingBill/listCustomerByCardNo.action?cardNo=' + cardNum,
                    dataType:'json',
                    success:function(result) {
                        if (result.success) {
                            // 1-买家 2-卖家
                            if(result.data.customerCharacterType=='buyer_character_type'){
                                $('#buyerCardNo').val(cardNum);
                                $('#show_buyer_name_by_card_name').val(result.data.customerName);
                            }else if(result.data.customerCharacterType=='business_user_character_type'){
                                $('#sellerCardNo').val(cardNum);
                                $('#show_seller_name_by_card_name').val(result.data.customerName);
                            }
                        }else{
                             bs4pop.alert(result.message, {type: "error"});
                            $('#buyerCardNo').val('');
                            $('#show_buyer_name_by_card_name').val('');
                            $('#sellerCardNo').val('');
                            $('#show_seller_name_by_card_name').val('');
                        }
                    },
                    error:function(){

                    }
                });
            }else{
                bs4pop.alert("未读取到卡号!", {type: 'error'});
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
        serviceUrl: '/farmerTradingBill/listOperatorByKeyword.action',
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
                                value: dataItem.userName + '|' + dataItem.realName
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
    lay('.settletime').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            , type: 'datetime'
            , theme: '#007bff'
            , min: getLastYearYestdy(new Date())
            , max: timeStamp2String(new Date().getTime()),
            done: function (value, date) {
                isStartEndDatetime(this.elem, value);
            }
        });
    });
    // 时间范围
    lay('.settletimeEnd').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            , type: 'datetime'
            , theme: '#007bff'
            , min: getLastYearYestdy(new Date())
            , max: timeStamp2String(new Date().getTime()),
            done: function (value, date) {
                isStartEndDatetime(this.elem, value);
            }
            ,ready: function(date){
                $(".layui-laydate-footer [lay-type='datetime'].laydate-btns-time").click();
                $(".laydate-main-list-0 .layui-laydate-content li ol li:last-child").click();
                $(".layui-laydate-footer [lay-type='date'].laydate-btns-time").click();
            }
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
    return strYear + "-" + strMonth + "-" + strDay+' 00:00:00';
    }

    function timeStamp2String(time) {
    var datetime = new Date();
    datetime.setTime(time);
    var year = datetime.getFullYear();
    var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
    var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
    return year + "-" + month + "-" + date + ' 23:59:59';
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
        // 获取表格显示列
        localStorage.setItem('weightingBillGridVisibleColumns', JSON.stringify({data: getVisibleColumnsDataHandler('weightingBillGrid', _grid)}));
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {url: '/farmerTradingBill/listPage.action', pageSize: parseInt(size), columns: JSON.parse(localStorage.getItem('weightingBillGridVisibleColumns')).data});
    });

    // -------------切换列显隐时保存隐藏列列头
    _grid.on('column-switch.bs.table', function(){
        // 保存隐藏列头
        saveHiddenColumnsHandler('weightingBillGrid', _grid);
    })


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
        if (rows[0].state!=1&&rows[0].state!=2) {
            bs4pop.alert('该单据当前状态不能进行作废操作！');
            return;
        }
        bs4pop.confirm(" 确定作废当前单据吗？", {title: "确认提示"}, function (sure) {
            if (sure) {
                $('#_modal .modal-body').load("/farmerTradingBill/operatorInvalidate.html?id="+rows[0].id);
                _modal.find('.modal-title').text('信息确认');
                $("#_modal").modal();
            }
        });
    }
    function cancelHandler(){
        window.location.reload();
    }
    function invalidateHandler(){
                    $.ajax({
                    type: "POST",
                    dataType: "json",
                    url:'/farmerTradingBill/operatorInvalidate.action',
                    data: $('#validatePasswordForm').serialize(),
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            if (data.data&&data.data.locked) {
                                window.location.reload();
                            }
                            return;
                        }
                        window.location.reload();
                    },
                    error: function (data) {
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
        if (rows[0].state!=4) {
            bs4pop.alert('该单据当前状态不能进行撤销操作！');
            return;
        }
        bs4pop.confirm(" 确定撤销当前单据吗？", {title: "确认提示"}, function (sure) {
            if (sure) {
                $('#_modal .modal-body').load("/farmerTradingBill/operatorWithdraw.html?id="+rows[0].id);
                _modal.find('.modal-title').text('信息确认');
                $("#_modal").modal();
            }
        });
    }

    function withdrawHandler(){
                    $.ajax({
                    type: "POST",
                    dataType: "json",
                    url:'/farmerTradingBill/operatorWithdraw.action',
                    data: $('#validatePasswordForm').serialize(),
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            if (data.data&&data.data.locked) {
                                window.location.reload();
                            }
                            return;
                        }
                        window.location.reload();
                    },
                    error: function (data) {
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

    function openDetailHandler() {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        dia = bs4pop.dialog({
            title: '过磅单详情',// 对话框title
            content: '${contextPath}/farmerTradingBill/weighingStatement/detail.html?id='+rows[0].statement.id, // 对话框内容，可以是
            width: '98%',// 宽度
            height: '95%',// 高度
            isIframe: true,// 默认是页面层，非iframe
            backdrop: 'static',
          btns: [{ label: '关闭', className: 'btn btn-secondary', onClick(e, $iframe) { } }]
        });
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
        	exportData:params.exportData,
            rows: params.limit,   // 页面大小
            page: ((params.offset / params.limit) + 1) || 1, // 页码
        };
        if (params.sort) {
        	temp.sort=params.sort;
        }else{
        	temp.sort = sortMap[_grid.bootstrapTable('getOptions').sortName];
        }
        if(params.order){
        	temp.order=params.order;
        }else{
        	temp.order=_grid.bootstrapTable('getOptions').sortOrder;
        }
        var aaa=$.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
        return aaa;
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
    
    var tableSortName,tableSortOrder;
    var sortMap={};
    var displayMap={};
    $('#grid th').each(function(index,item){
       if($(item).attr('data-field')&&$(item).attr('data-sort-name')){
            sortMap[$(item).attr('data-field')]=$(item).attr('data-sort-name');
            displayMap[$(item).attr('data-field')]=$(item).text();
       }
    });
    console.log(sortMap);
    
    $(function(){
        $('#grid').on('click-row.bs.table', function (e, row, $element, field) {
            if(row.statement.state == ${@com.dili.orders.domain.WeighingStatementState.UNPAID.getValue()}){
                $("#btn_invalidate").attr("disabled", false);
                $("#btn_invalidate").removeClass("btn_css_disabled");
                $("#btn_withdraw").attr("disabled", "btn_css_disabled");
                $("#btn_withdraw").addClass("btn_css_disabled");
                $("#btn_reprint").attr("disabled", "disabled");
                $("#btn_reprint").addClass("btn_css_disabled");
            }else if(row.statement.state == ${@com.dili.orders.domain.WeighingStatementState.PAID.getValue()}){
                $("#btn_invalidate").attr("disabled", "disabled");
                $("#btn_invalidate").addClass("btn_css_disabled");
                $("#btn_withdraw").attr("disabled", false);
                $("#btn_withdraw").removeClass("btn_css_disabled");
                $("#btn_reprint").attr("disabled", false);
                $("#btn_reprint").removeClass("btn_css_disabled");
            }else if(row.statement.state == ${@com.dili.orders.domain.WeighingStatementState.FROZEN.getValue()}){
                $("#btn_invalidate").attr("disabled", false);
                $("#btn_invalidate").addClass("btn_css_disabled");
                $("#btn_withdraw").attr("disabled", "disabled");
                $("#btn_withdraw").removeClass("btn_css_disabled");
                $("#btn_reprint").attr("disabled", false);
                $("#btn_reprint").removeClass("btn_css_disabled");
            }else {
                $("#btn_invalidate").attr("disabled", "disabled");
                $("#btn_invalidate").addClass("btn_css_disabled");
                $("#btn_withdraw").attr("disabled", "disabled");
                $("#btn_withdraw").addClass("btn_css_disabled");
                $("#btn_reprint").attr("disabled", "disabled");
                $("#btn_reprint").addClass("btn_css_disabled");
            }
        })
    });
    
    /**
	 * ***************************************自定义事件区
	 * end*************************************
	 */
    </script>