$('span.ui-icon.ui-icon-trash').click(function () {
	var key = $(this).parent().parent().find('.resource-type').html();
	$.ajax({
		url: 'slingcacheinclude',
		type: 'POST',
		context: $(this),
		data: {
			'${parameter.cacheName}': key,
			'${parameter.action}': '${action.delete}'
		}
	}).done(function () {
		$(this).siblings('span').text('Cache cleared.');
	}).fail(function () {
		$(this).siblings('span').text('There were some errors. Please see log file for details.');
	});
});