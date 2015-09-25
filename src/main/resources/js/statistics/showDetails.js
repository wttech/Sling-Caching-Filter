$('div.bIcon.ui-icon-triangle-1-e').on('click', function () {
	var key = $(this).next().text();
	$.ajax({
		url: 'slingcacheinclude',
		type: 'POST',
		context: $(this),
		data: {
			'${parameter.cacheName}': key,
			'${parameter.action}': '${action.showDetails}'
		}
	}).done(function (data) {
		$(this).siblings('div.cache-keys').append(data);
		$(this).parent().next().text($(data).find('tr').length - 1);
	}).fail(function () {
		$(this).siblings('div.cache-keys').text('Unable to load cache details. Please see log file for details.');
	});
	$(this).siblings('div.cache-keys').toggle();
	$(this).toggleClass('ui-icon-triangle-1-e ui-icon-triangle-1-s');
	$(this).off('click');
});
