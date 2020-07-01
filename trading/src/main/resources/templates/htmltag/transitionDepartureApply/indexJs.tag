<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;


    //时间范围
    lay('.laydatetime').each(function () {
        laydate.render({
            elem: this
            , trigger: 'click'
            , range: false
            ,type: 'datetime'
        });
    });




    // 客户名称
    var customerNameAutoCompleteOption = {
        serviceUrl: '/customer/listNormal.action',
        paramName: 'likeName',
        displayFieldName: 'name',
        showNoSuggestionNotice: true,
        noSuggestionNotice: '不存在，请重新输入！',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        debugger
                        return $.extend(dataItem, {
                                value: dataItem.name + '（' + dataItem.contactsPhone + '，' + dataItem.certificateNumber.substr(-4) + '，' + dataItem.certificateAddr +'）'
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

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {url: '/transitionDepartureApplyController/listByQueryParams.action', pageSize: parseInt(size)});
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
        dia = bs4pop.dialog({
            title: '转离场申请',//对话框title
            content: '${contextPath}/transitionDepartureApplyController/add.html', //对话框内容，可以是 string、element，$object
            width: '60%',//宽度
            height: '600px',//高度
            isIframe: true,//默认是页面层，非iframe
            btns: [{label: '取消',className: 'btn btn-secondary',onClick(e, $iframe){

                }
            }, {label: '确定',className: 'btn btn-primary',onClick(e, $iframe){
                    let diaWindow = $iframe[0].contentWindow;
                    bui.util.debounce(diaWindow.saveOrUpdateHandler,1000,true)()
                    return false;
                }
            }]
        });

    }

    /**
     * 打开查看
     * @param id
     */
    function openViewHandler(id) {
        if(!id){
            //获取选中行的数据
            let rows = _grid.bootstrapTable('getSelections');
            if (null == rows || rows.length == 0) {
                bs4pop.alert('请选中一条数据');
                return;
            }
            id = rows[0].id;
        }


        dia = bs4pop.dialog({
            title: '申请单详情',
            content: '/transitionDepartureApplyController/getOneByID.action?id='+id,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '80%',
            height : '95%',
            btns: [{label: '关闭', className: 'btn-secondary', onClick(e) {}}]
        });
    }


    /**
     * 打开修改窗口
     */
   /* function openUpdateHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        $("#_modal").modal("show");
        if(rows[0].id){
			$('#_modal .modal-body').load("/carTypePublic/update.html?id=" + rows[0].id + "&carTypeId=" + rows[0].car_type_id + "&name=" + rows[0].carTypeName + "&number=" + rows[0].carTypeNumber);
        }else{
        	$('#_modal .modal-body').load("/carTypePublic/update.html?" + "carTypeId=" + rows[0].car_type_id + "&name=" + rows[0].carTypeName + "&number=" + rows[0].carTypeNumber);
        }
		
        _modal.find('.modal-title').text('业务属性设置');
    }*/

    function openDeleteHandler(id) {
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        bs4pop.confirm("确定要删除吗", {title: "确认提示"}, function (sure) {
            if (sure) {
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: '/carTypePublic/delete.action',
                    data: {id: rows[0].id},
                    success: function (data) {
                        bui.loading.hide();
                        if (data.code != '200') {
                            bs4pop.alert(data.message, {type: 'error'});
                            return;
                        }
                        // bs4pop.alert('成功', {type: 'success '}, function () {
                        //     window.location.reload();
                        // });
                        window.location.reload();
                    },
                    error: function () {
                        bui.loading.hide();
                        bs4pop.alert("车型删除失败!", {type: 'error'});
                    }
                });
            }
        });
    }

    /**
     * 禁启用操作
     * @param enable 是否启用:true-启用
     * @param id
     */
   /* function doEnableHandler(enable, id) {
        var opType ="";
        if(enable == 1){
            opType = "enable";
        }
        if(enable == 2){
            opType = "disable";
        }
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        //table选择模式是单选时可用
        let msg = enable == 1 ? '确定要启用该车型吗？' : '确定要禁用该车型吗？';
        bs4pop.confirm(msg, undefined, function (sure) {
            if (sure) {
                bui.loading.show('努力提交中，请稍候。。。');
                $.ajax({
                    type: "POST",
                    url: "${contextPath}/carTypePublic/updateStatus.action",
                    data: {id: rows[0].id, status: enable,opType:opType, "carType.name": rows[0].carTypeName},
                    processData: true,
                    dataType: "json",
                    async: true,
                    success: function (data) {
                        bui.loading.hide();
                        if (data.success) {
                            _grid.bootstrapTable('refresh');
                            $("#btn_disabled").removeClass("btn_css_disabled");
                            $("#btn_enabled").removeClass("btn_css_disabled");
                            _modal.modal('hide');
                        } else {
                            bs4pop.alert(data.result, {type: 'error'});
                        }
                    },
                    error: function () {
                        bui.loading.hide();
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    }
                });
            }
        })
    }*/


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