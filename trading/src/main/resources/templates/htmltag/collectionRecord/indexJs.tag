<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;


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

    function swipeBuyerCard(){
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
                        $('#buyerCardNo').val(cardNum);
                        $('#accountBuyerId').val(result.data.accountId);
                        $('#show_buyer_name_by_card_name').val(result.data.customerName);
                    }else{
                        bs4pop.alert(result.message, {type: "error"});
                        $('#buyerCardNo').val('');
                        $('#accountBuyerId').val('');
                        $('#show_buyer_name_by_card_name').val('');
                    }
                },
                error:function(){

                }
            });
        }else{
            bs4pop.alert("未读取到卡号!", {type: 'error'});
        }
    }
    function swipeSellerCard(){
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
                        $('#sellerCardNo').val(cardNum);
                        $('#accountSellerId').val(result.data.accountId);
                        $('#show_seller_name_by_card_name').val(result.data.customerName);
                    }else{
                        bs4pop.alert(result.message, {type: "error"});
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
            pageSize: parseInt(size)
        });
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
        _grid.bootstrapTable('refreshOptions', {
            url: '/collectionRecord/listByQueryParams.action',
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
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }
    /*****************************************函数区 end**************************************/
    function dataFormatterTip(value,row,index) {
        if (value) {
            return "<span data-toggle='tooltip' data-placement='left' title='" + value + "'>" + value + "</span>";
        } else {
            return "";
        }
    }
</script>