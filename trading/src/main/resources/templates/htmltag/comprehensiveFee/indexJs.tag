<script>
    /*********************变量定义区 begin*************/
    //行索引计数器
    // 如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    let _modal = $('#_modal');
    var dia;


    //时间范围
    lay('.laydatetime').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            , type: 'datetime'
        });
    });


    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/customer/listNormal.action',
        paramName: 'likeName',
        displayFieldName: 'code',
        showNoSuggestionNotice: true,
        minChars: 2,
        width: 'flex',
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
            $('#show_customer_name').val(suggestion.name);
        }
    };

    // 结算员名称
    var operatorNameAutoCompleteOption = {
        serviceUrl: '/comprehensiveFee/listOperatorByKeyword.action',
        paramName: 'keyword',
        displayFieldName: 'realName',
        showNoSuggestionNotice: true,
        minChars: 2,
        //width: 'flex',
        noSuggestionNotice: '结算员不存在',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                //value: dataItem.realName + '（' + dataItem.serialNumber + '）'
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
            /*$('#show_operator_name').val(suggestion.realName);*/
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
            url: '/comprehensiveFee/listByQueryParams.action',
            pageSize: parseInt(size)
        });
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        dia = bs4pop.dialog({
            title: '检测收费单',//对话框title
            content: '${contextPath}/comprehensiveFee/add.html', //对话框内容，可以是 string、element，$object
            width: '880px',//宽度
            height: '500px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });

    }

    /**
     * 打开确认支付页面
     * */
    function verificationUsernamePassword(id) {
        dia = bs4pop.dialog({
            title: '支付确认',//对话框title
            content: '${contextPath}/comprehensiveFee/verificationUsernamePassword.action?id=' + id, //对话框内容，可以是 string、element，$object
            width: '400px',//宽度
            height: '390px',//高度
            backdrop: 'static',
            isIframe: true//默认是页面层，非iframe
        });

    }

    /**
     * 补打
     */
    function openPrintHandler() {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_orderStatus != 2) {
            bs4pop.alert('该单据当前状态不能进行补打操作！');
            return;
        }
        //查询comprehensiveFee单信息
        $.ajax({
            type: "POST",
            dataType: "json",
            url: '/comprehensiveFee/printOneById.action',
            data: {id: rows[0].id},
            success: function (data) {
                bui.loading.hide();
                if (data.code == '200') {
                    let comprehensiveFee= data.data;
                    //查询余额
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        url: '/comprehensiveFee/queryAccountBalance.action',
                        data: {customerCardNo: rows[0].customerCardNo},
                        success: function (res) {
                            bui.loading.hide();
                            if (res.code == '200') {
                                //调用c端打印
                                comprehensiveFee.balance = res.data.accountFund.balance;
                                callbackObj.printDirect(JSON.stringify(comprehensiveFee), "CheckRechargeDocument");
                            }
                        },
                        error: function () {
                            bui.loading.hide();
                            bs4pop.alert("打印失败!", {type: 'error'});
                        }
                    });

                }
            },
            error: function () {
                bui.loading.hide();
                bs4pop.alert("打印失败!", {type: 'error'});
            }
        });

    }

    /*
    * 调用撤销功能
    * */
    function openUpdateHandler() {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        if (rows[0].$_orderStatus != 2) {
            bs4pop.alert('该单据当前状态不能进行撤销操作！');
            return;
        }

        bs4pop.confirm(" <div style='font-size: large' align='center' >确定撤销当前单据？</div>", {
            title: "信息确认", btns: [
                {
                    label: '确定', className: 'btn btn-primary', onClick(e, $iframe) {
                        $('#_modal .modal-body').load("${contextPath}/comprehensiveFee/revocatorPage.html?id=" + rows[0].id);
                        _modal.find('.modal-title').text('撤销校验');
                        $("#_modal").modal();
                    }
                }, {
                    label: '取消', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]
        }, function (sure) {});
    }

    /*
    *
    * 撤销实现
    * */
    function revocator() {
        $.ajax({
            type: "POST",
            dataType: "json",
            url: '/comprehensiveFee/revocator.action',
            data: $('#UserPasswordForm').serialize(),
            success: function (data) {
                bui.loading.hide();
                if (data.code != '200') {
                    bs4pop.alert(data.message, {type: 'error'});
                    return;
                } else{
                    bs4pop.alert("退款成功","",function (){window.location.reload();});
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

    /**
     * 打开查看
     * @param id
     */
    function openViewHandler(id) {
        if (!id) {
            //获取选中行的数据
            let rows = _grid.bootstrapTable('getSelections');
            if (null == rows || rows.length == 0) {
                bs4pop.alert('请选中一条数据');
                return;
            }
            id = rows[0].id;
        }


        dia = bs4pop.dialog({
            title: '检测收费单详情',
            content: '/comprehensiveFee/view.html?id=' + id,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '60%',
            height: '95%',
            btns: [{
                label: '关闭', className: 'btn-secondary', onClick(e) {
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

    /** 点击编号查看事件*/
    function view(value, row, index) {
        return [
            '<a href=javascript:openViewHandler('+row.id+')>'+value+'</a>'
        ].join("")
    }
    /*****************************************函数区 end**************************************/

</script>