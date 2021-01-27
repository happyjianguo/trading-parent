<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    var dia;

    function openViewHandler(url) {
        dia = bs4pop.dialog({
            title: '查看',
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

    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {
            url: '/collectionRecord/weighingBillListQuery.action',
            pageSize: parseInt(size)
        });
    });

    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/

    /**
     * 查询处理
     */
    function queryDataHandler() {
        // if($('#createdStart').val()===''){
        //     bs4pop.alert('查询日期必填', {type: 'error'});
        //     return false;
        // }
        // if (!$('#queryForm').validate().form()) {
        //     return false;
        // }
        _grid.bootstrapTable('refresh');
    }


    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        debugger;
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        };
        return JSON.stringify($.extend({}, temp, bui.util.bindGridMeta2Form('grid', 'queryForm')))
    }

</script>