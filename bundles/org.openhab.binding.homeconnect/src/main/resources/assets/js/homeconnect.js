/* globals Chart:false, feather:false, requests:false */

(function () {
    'use strict'

    feather.replace();

    $(".redirectUri").text(window.location.href.substring(0, window.location.href.lastIndexOf('/homeconnect') + 12));
    $(".redirectUriInput").val(window.location.href.substring(0, window.location.href.lastIndexOf('/homeconnect') + 12));

    $('#apiDetailModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var thingId = button.data('thing-id');
        var action = button.data('api-action');
        var titleText = button.data('title');
        var modal = $(this);
        var title = modal.find('.modal-title');
        var subTitle = modal.find('.modal-subtitle');
        var responseBodyElement = modal.find('.modal-response-body');

        responseBodyElement.text('Loading...');
        title.text(titleText);
        subTitle.text(thingId);
        modal.modal('handleUpdate');

        let jqxhr = $.get('appliances?thingId=' + thingId + '&action=' + action, function (data) {
            responseBodyElement.text(JSON.stringify(data, null, '\t'));
        });
        jqxhr.fail(function (data) {
            responseBodyElement.text(JSON.stringify(data, null, '\t'));
        })
        jqxhr.always(function () {
            modal.modal('handleUpdate');
        });
    })

    $('#rawCommandDetailModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var thingId = button.data('thing-id');
        var haId = button.data('ha-id');

        var modal = $(this);
        var subTitle = modal.find('.modal-subtitle');
        var inputPath = modal.find('#raw-path');
        var inputBody = modal.find('#raw-request-body');
        var submit = modal.find('#raw-submit');
        var responseBodyElement = modal.find('.modal-response-body');
        var responseTitle = modal.find('.raw-response-header');

        subTitle.text(thingId);
        responseBodyElement.text('');
        responseTitle.hide();
        inputPath.val('/api/homeappliances/' + haId + '/programs/active')
        modal.modal('handleUpdate');

        submit.click(function () {
            responseBodyElement.text('Loading...');
            let jqxhr = $.post('appliances?thingId=' + thingId + '&action=put-raw&path=' + inputPath.val(),
                               inputBody.val(), function (data) {
                    responseBodyElement.text(JSON.stringify(data, null, '\t'));
                    responseTitle.show();
                });
            jqxhr.fail(function (data) {
                responseBodyElement.text(JSON.stringify(data, null, '\t'));
                responseTitle.show();
            })
            jqxhr.always(function () {
                modal.modal('handleUpdate');
            });
        });
    })

    $('#rawGetDetailModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var thingId = button.data('thing-id');
        var haId = button.data('ha-id');

        var modal = $(this);
        var subTitle = modal.find('.modal-subtitle');
        var inputPath = modal.find('#raw-get-path');
        var submit = modal.find('#raw-get-submit');
        var responseBodyElement = modal.find('.modal-response-body');
        var responseTitle = modal.find('.raw-response-header');

        subTitle.text(thingId);
        responseBodyElement.text('');
        responseTitle.hide();
        inputPath.val('/api/homeappliances/' + haId + '/programs')
        modal.modal('handleUpdate');

        submit.click(function () {
            responseBodyElement.text('Loading...');
            let jqxhr = $.post('appliances?thingId=' + thingId + '&action=get-raw&path=' + inputPath.val(), function (data) {
                responseBodyElement.text(JSON.stringify(data, null, '\t'));
                responseTitle.show();
            });
            jqxhr.fail(function (data) {
                responseBodyElement.text(JSON.stringify(data, null, '\t'));
                responseTitle.show();
            })
            jqxhr.always(function () {
                modal.modal('handleUpdate');
            });
        });
    })

    $('#requestDetailModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var requestId = button.data('request-id');
        var request = requests.find(item => item.id == requestId);
        var requestHeader = request.homeConnectRequest.header;
        var requestBody = request.homeConnectRequest.body;
        var modal = $(this);
        var requestBodyElement = modal.find('.modal-request-body');
        var title = modal.find('.modal-title');
        var titleBadgeElement = modal.find('.modal-title-badge');
        var responseBodyElement = modal.find('.modal-response-body');
        var requestHeaderElement = modal.find('.modal-request-header');
        var responseHeaderElement = modal.find('.modal-response-header');

        title.text(request.homeConnectRequest.method + ' ' + request.homeConnectRequest.url);

        if (requestBody) {
            requestBodyElement.text(requestBody);
            requestBodyElement.removeClass('text-muted')
        } else {
            requestBodyElement.text('Empty request body');
            requestBodyElement.addClass('text-muted')
        }

        if (request.homeConnectResponse && request.homeConnectResponse.body) {
            responseBodyElement.text(request.homeConnectResponse.body);
            responseBodyElement.removeClass('text-muted')
        } else {
            responseBodyElement.text('Empty response body');
            responseBodyElement.addClass('text-muted')
        }

        titleBadgeElement.empty();
        if (request.homeConnectResponse) {
            var statusCode = request.homeConnectResponse.code;
            titleBadgeElement.text(statusCode);
            titleBadgeElement.removeClass('badge-success');
            titleBadgeElement.removeClass('badge-danger');
            titleBadgeElement.removeClass('badge-warning');

            if (statusCode >= 300 && statusCode != 404) {
                titleBadgeElement.addClass('badge-danger');
            } else if (statusCode >= 200 && statusCode < 300) {
                titleBadgeElement.addClass('badge-success');
            } else {
                titleBadgeElement.addClass('badge-warning');
            }
        }

        responseHeaderElement.empty();
        if (request.homeConnectResponse && request.homeConnectResponse.header) {
            var responseHeader = request.homeConnectResponse.header;
            Object.keys(responseHeader).forEach(key => {
                console.log(`key=${key}  value=${responseHeader[key]}`);
                responseHeaderElement.append($(`<dt class="col-sm-4">${key}</dt>`));
                responseHeaderElement.append($(`<dd class="col-sm-8 text-break">${responseHeader[key]}</dd>`));
                responseHeaderElement.append($('<div class="w-100"></div>'));
            });
        }

        requestHeaderElement.empty();
        Object.keys(requestHeader).forEach(key => {
            console.log(`key=${key}  value=${requestHeader[key]}`);
            requestHeaderElement.append($(`<dt class="col-sm-4">${key}</dt>`));
            requestHeaderElement.append($(`<dd class="col-sm-8 text-break">${requestHeader[key]}</dd>`));
            requestHeaderElement.append($('<div class="w-100"></div>'));
        });

        modal.modal('handleUpdate');
    })

    $('.reload-page').click(function () {
        location.reload();
    });
}())
