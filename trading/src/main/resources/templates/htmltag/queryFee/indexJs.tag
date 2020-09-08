<script>
    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/queryFee/listCustomerByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 2,
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
            $('#show_customer_name').val(suggestion.name);
        }
    };

    // 结算员名称
    var operatorNameAutoCompleteOption = {
        serviceUrl: '/queryFee/listOperatorByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'id',
        showNoSuggestionNotice: true,
        noSuggestionNotice: '结算员不存在',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.realName + '（' + dataItem.serialNumber + '）'
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
            $('#show_operator_name').val(suggestion.realName);
        }
    };

    // 卡号
    var customerCardQueryAutoCompleteOption = {
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
                    //888810054629
                }
            }else{
                return false;
            }
        },
        selectFn: function (suggestion) {
            $('#show_customer_card_no').val(suggestion.customerName);
            $('#customerCardNo').val(suggestion.cardNo);
        }
    };

    //时间范围
    lay('.laydatetime').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            , type: 'datetime'
        });
    });

    /*********************变量定义区 begin*************/
    //行索引计数器
    //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/queryFee/listByQueryParams.action',
            //onLoadSuccess: toFixChargeAmount,
            pageSize: parseInt(size)
        });
    });

    /*执行前改变数据*/
    function toFixChargeAmount(data) {
        var rows=data.rows;
        for(var i=0;i<rows.length;i++){
            var chargeAmount=rows[i].chargeAmount;
            rows[i].chargeAmount=(chargeAmount/100).toFixed(2);
        }
        _grid.bootstrapTable('load',data);
    }
    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        dia = bs4pop.dialog({
            title: '查询收费',//对话框title
            content: '${contextPath}/queryFee/add.html', //对话框内容，可以是 string、element，$object
            width: '60%',//宽度
            height: '95%',//高度
            backdrop: 'static',
            isIframe: true,//默认是页面层，非iframe
            btns: [
                {
                    label: '下一步', className: 'btn btn-primary', onClick(e, $iframe) {
                        let diaWindow = $iframe[0].contentWindow;
                        bui.util.debounce(diaWindow.saveOrUpdateHandler, 1000, true)()
                        return false;
                       //verificationUsernamePassword();
                    }
                }, {
                    label: '取消', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]
        });

    }

    function verificationUsernamePassword(id) {
        dia = bs4pop.dialog({
            title: '支付确认',//对话框title
            content: '${contextPath}/queryFee/verificationUsernamePassword.action?id=' + id, //对话框内容，可以是 string、element，$object
            width: '60%',//宽度
            height: '60%',//高度
            backdrop: 'static',
            isIframe: true,//默认是页面层，非iframe
            btns:
                [{
                    label: '付款', className: 'btn btn-primary', onClick(e, $iframe) {
                        let diaWindow = $iframe[0].contentWindow;
                        bui.util.debounce(diaWindow.pay, 1000, true)()
                        return false;
                    }
                },
                    {
                    label: '取消', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]
        });

    }

    /**
     * 查询处理
     */
    function queryDataHandler() {
        _grid.bootstrapTable('refresh');
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

</script>