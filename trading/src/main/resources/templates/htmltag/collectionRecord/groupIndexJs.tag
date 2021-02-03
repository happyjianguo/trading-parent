<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia, diaPay;


    var buyerNameQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByKeyword.action',
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
                                value: dataItem.code + ' | ' + dataItem.name + ' | ' + dataItem.contactsPhone + ' | '+dataItem.id
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
            $('#buyerName').val(suggestion.name);
            $('#buyerIdI').val(suggestion.id);

            let targetId = $('#buyerCardNoI');
            targetId.empty();
            $.ajax({
                type : "POST",
                dataType : "json",
                url : '/orderCommon/cardList.action',
                data : {customerId : suggestion.id},
                success : function(res) {
                    let data = res.data;
                    if (data.length == 1) {
                        targetId.html('<option value="'+ data[0].cardNo  +'" selected>' + data[0].cardNo + '</option>');
                        getBuyerAccount(data[0].cardNo);
                    } else {
                        let str = '<option value="">请选择</option>';
                        $.each(data, function (i, item) {
                            str += '<option value="'+ item.cardNo  +'">' + item.cardNo + '</option>'
                        });
                        targetId.html(str);
                    }
                },
                error : function() {
                    bui.loading.hide();
                    bs4pop.alert("客户卡获取失败!", {
                        type : 'error'
                    });
                }
            });
        }
    };

    $('#buyerCardNoI').on('change', function () {
        getBuyerAccount($(this).val());
    })

    $('#sellerCardNoI').on('change', function () {
        getSellerAccount($(this).val());
    })

    function getBuyerAccount(cardNo){
        $.ajax({
            type: "POST",
            data: {cardNo: cardNo},
            dataType: "json",
            url: "/orderCommon/oneByCardNo.action",
            async: true,
            success: function (res) {
                if (res.code == "200") {
                    $('#accountBuyerIdI').val(res.data.accountInfo.accountId);
                    $('#buyerBalance').val(res.data.accountFund.availableAmount);
                    $('#buyerBalanceYuan').val(parseFloat(res.data.accountFund.availableAmount / 100).toFixed(2));
                } else {
                    bui.loading.hide();
                    bs4pop.alert(res.result, {type: 'error'});
                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("根据卡号获取账户信息失败!", {type: 'error'});
            }
        });
    }
    function getSellerAccount(cardNo){
        $.ajax({
            type: "POST",
            data: {cardNo: cardNo},
            dataType: "json",
            url: "/orderCommon/oneByCardNo.action",
            async: true,
            success: function (res) {
                if (res.code == "200") {
                    $('#sellerName').val(res.data.accountInfo.customerName);
                    $('#accountsellerIdI').val(res.data.accountInfo.accountId);
                } else {
                    bui.loading.hide();
                    bs4pop.alert(res.result, {type: 'error'});
                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("根据卡号获取账户信息失败!", {type: 'error'});
            }
        });
    }

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
                url:'${contextPath!}/weighingBill/listCustomerByCardNo.action?cardNo=' + cardNum,
                dataType:'json',
                success:function(result) {
                    if (result.success) {
                        // 1-买家 2-卖家
                        if(result.data.customerCharacterType=='buyer_character_type'){
                            $('#buyerCardNo').val(cardNum);
                            $('#buyerName').val(result.data.customerName);
                            $('#accountBuyerId').val(result.data.accountId);
                            $('#buyerIdI').val(result.data.customerId);
                            $('#show_buyer_name_by_card_name').val(result.data.customerName);

                            let targetId = $('#buyerCardNoI');
                            targetId.empty();
                            targetId.html('<option value="'+ cardNum  +'" selected>' + cardNum + '</option>');
                            getBuyerAccount(cardNum);

                        }else if(result.data.customerCharacterType=='business_user_character_type'){
                            $('#sellerCardNo').val(cardNum);
                            $('#sellerName').val(result.data.customerName);
                            $('#accountSellerId').val(result.data.accountId);
                            $('#accountSellerIdI').val(result.data.accountId);
                            $('#sellerIdI').val(result.data.customerId);
                            $('#show_seller_name_by_card_name').val(result.data.customerName);

                            let targetId = $('#sellerCardNoI');
                            targetId.empty();
                            targetId.html('<option value="'+ cardNum  +'" selected>' + cardNum + '</option>');
                            getSellerAccount(cardNum);
                        }
                    }else{
                        bs4pop.alert(result.message, {type: "error"});
                        $('#buyerCardNo').val('');
                        $('#accountBuyerId').val('');
                        $("#buyerCardNoI").empty();
                        $("#sellerCardNoI").empty();
                        $('#show_buyer_name_by_card_name').val('');
                        $('#sellerCardNo').val('');
                        $('#accountSellerId').val('');
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

    var sellerNameQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByKeyword.action',
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
            $('#sellerName').val(suggestion.name);
            $('#sellerIdI').val(suggestion.id);

            let targetId = $('#sellerCardNoI');
            targetId.empty();
            $.ajax({
                type : "POST",
                dataType : "json",
                url : '/orderCommon/cardList.action',
                data : {customerId : suggestion.id},
                success : function(res) {
                    let data = res.data;
                    if (data.length == 1) {
                        targetId.html('<option value="'+ data[0].cardNo  +'" selected>' + data[0].cardNo + '</option>');
                        getSellerAccount(data[0].cardNo);

                    } else {
                        let str = '<option value="">请选择</option>';
                        $.each(data, function (i, item) {
                            str += '<option value="'+ item.cardNo  +'">' + item.cardNo + '</option>'
                        });
                        targetId.html(str);
                    }
                },
                error : function() {
                    bui.loading.hide();
                    bs4pop.alert("客户卡获取失败!", {
                        type : 'error'
                    });
                }
            });
        }
    };


    var customerCardQueryAutoCompleteOption = {
        serviceUrl: '/weighingBill/listCustomerByCardNo.action',
        paramName: 'cardNo',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 12,
        width: 'flex',
        noSuggestionNotice: '无此客户, 请重新输入',
        transformResult: function (result) {
            if (result.success) {
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.customerName + "|" + dataItem.cardNo
                            }
                        );
                    })
                    //888810054629
                }
            } else {
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_customer_name').val(suggestion.customerName);
            $('#customerCardNo').val(suggestion.cardNo);
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

    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/customer/listNormal.action',
        paramName: 'likeName',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        width: '320px',
        noSuggestionNotice: '不存在，请重新输入！',
        transformResult: function (result) {
            if (result.success) {
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.code + ' | ' + dataItem.name + ' | ' + dataItem.contactsPhone
                            }
                        );
                    })
                }
            } else {
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#showCustomerName').val(suggestion.name);
        }
    };

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            // url: '/collectionRecord/groupListByQueryParams.action',
            pageSize: parseInt(size)
        });
        showOrHiddenColumn();
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/


    /**
     * 查询处理
     */
    function queryDataHandler() {
        // if (!$('#queryForm').validate().form()) {
        //     return false;
        // }
        // _grid.bootstrapTable('refresh');
        // let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        // size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/collectionRecord/groupListByQueryParams.action',
            // pageSize: parseInt(size)
            pageNum:1
        });
    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        };
        showOrHiddenColumn();
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }
    /*****************************************函数区 end**************************************/

    function showOrHiddenColumn(){
        $('#grid').bootstrapTable('hideColumn', 'ids');
    }

    // 表格列格式化
    function agentFormatter(value, row, index, field){
        if (value == undefined) {
            return '-';
        }
        let str = '<a href="javascript:;" class="text-primary">' + value + '</a>'
        return str
    }

    // 列点击事件
    _grid.on('click-cell.bs.table', function(field, value, row, $element){
        if (value === 'cnt' && row !== undefined){
            let collectionRecordIds = "["+$element.ids+"]";
            localStorage.setItem("collectionRecordIds",collectionRecordIds);
            openViewHandler("${contextPath}/collectionRecord/weighingBillList.html");
        }
    })
    function openViewHandler(url) {
        dia = bs4pop.dialog({
            title: '关联结算单',
            content: url,
            width: '100%',//宽度
            height: '100%',//高度
            isIframe: true,
            btns: [{
                label: '关闭', className: 'btn btn-secondary', onClick(e, $iframe) {

                }
            }]

        });
    }

    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        //获取选则了的项
        let rows = _grid.bootstrapTable('getSelections');
        if(null == rows || rows.length == 0){
            bs4pop.alert("请选择一条或多条数据进行回款!", {type: 'error'});
            return false;
        }
        let fundItems = [];
        if (null != rows && rows.length > 0) {
            for (let i = 0; i < rows.length; i++) {
                fundItems.push(rows[i].ids);
            }
        }
        let money = parseFloat(0);
        if (null != rows && rows.length > 0) {
            for (let i = 0; i < rows.length; i++) {
                money+=parseFloat(rows[i].amount);
            }
        }

        let dateStr = [];
        if (null != rows && rows.length > 0) {
            for (let i = 0; i < rows.length; i++) {
                dateStr.push(rows[i].time);
            }
        }


        localStorage.setItem("idList",fundItems.toString());
        localStorage.setItem("amount",money.toFixed(2));

        //设置买家相关信息
        localStorage.setItem("buyerName",$('#buyerName').val());
        localStorage.setItem("buyerIdI",$('#buyerIdI').val());
        localStorage.setItem("accountBuyerIdI",$('#accountBuyerIdI').val());
        localStorage.setItem("buyerCardNoI",$("#buyerCardNoI option:selected").val());
        localStorage.setItem("buyerBalance",$('#buyerBalanceYuan').val())

        //设置卖家相关信息
        localStorage.setItem("sellerName",$('#sellerName').val());
        localStorage.setItem("sellerIdI",$('#sellerIdI').val());
        localStorage.setItem("accountsellerIdI",$('#accountsellerIdI').val());
        localStorage.setItem("sellerCardNoI",$("#sellerCardNoI option:selected").val());

        localStorage.setItem("dateStr",dateStr.toString());

        diaPay = bs4pop.dialog({
            title: '回款',//对话框title
            content: '${contextPath}/collectionRecord/add.html', //对话框内容，可以是 string、element，$object
            width: '500px',//宽度
            height: '700px',//高度
            isIframe: true,//默认是页面层，非iframe
            backdrop: 'static'
        });

    }
    let cnt=0;
    let moneyAmountSpan=0;
    _grid.on('check.bs.table', function (row, $element) {
        cnt+=1;
        moneyAmountSpan+=parseFloat($element.amount);
        $('#days').text(cnt);
        $('#moneySpan').text("￥："+moneyAmountSpan.toFixed(2));


    })

    _grid.on('check-all.bs.table', function (rowsAfter, rowsBefore) {
        moneyAmountSpan=0;
        if (null != rowsBefore && rowsBefore.length > 0) {
            for (let i = 0; i < rowsBefore.length; i++) {
                moneyAmountSpan+=parseFloat(rowsBefore[i].amount);
            }
            cnt=rowsBefore.length;
            $('#days').text(cnt);
            $('#moneySpan').text("￥："+moneyAmountSpan.toFixed(2));
        }
    })

    _grid.on('uncheck.bs.table', function (row, $element) {
        cnt-=1;
        moneyAmountSpan-=parseFloat($element.amount);
        moneyAmountSpan=parseFloat(parseFloat(moneyAmountSpan).toFixed(2));
        $('#days').text(cnt);
        $('#moneySpan').text("￥："+moneyAmountSpan.toFixed(2));
    })

    _grid.on('uncheck-all.bs.table', function (rowsAfter, rowsBefore) {
        moneyAmountSpan=0;
        cnt=0;
        $('#days').text('0');
        $('#moneySpan').text("￥："+'0.00');
    })



</script>