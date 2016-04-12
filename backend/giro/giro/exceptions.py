import rest_framework.exceptions as rest_exceptions
import logging
from rest_framework.views import exception_handler as default_exception_handler
import settings
import traceback


class ErrorCodes(object):
    UNKNOWN_ERROR = -1  # Default error code if no other code is specified.
    AUTH_FAILED = -2
    REQUIRED_FIELD_MISSING = -3
    INTERNAL_SERVER_ERROR = -4
    METHOD_NOT_ALLOWED = -5
    PARSE_ERROR = -6
    OPERATOR_EXISTS = -7
    OPERATOR_ACCOUNT_INVALID = -8
    PASSWORD_INVALID = -9
    INSPECTION_NOT_FOUND = -10


# Map of Rest Framework exceptions to DWS error codes.
ERR_CODE_MAP = {
    rest_exceptions.NotAuthenticated: ErrorCodes.AUTH_FAILED,
    rest_exceptions.APIException: ErrorCodes.INTERNAL_SERVER_ERROR,
    rest_exceptions.ParseError: ErrorCodes.PARSE_ERROR,
    rest_exceptions.MethodNotAllowed: ErrorCodes.METHOD_NOT_ALLOWED,
}


class RequiredFieldMissing(rest_exceptions.APIException):
    status_code = 400
    err_code = ErrorCodes.REQUIRED_FIELD_MISSING


class InternalServerError(rest_exceptions.APIException):
    status_code = 500
    err_code = ErrorCodes.INTERNAL_SERVER_ERROR


class OperatorExists(rest_exceptions.APIException):
    status_code = 400
    err_code = ErrorCodes.OPERATOR_EXISTS


# If operator account hasn't been confirmed or email doesn't exist in db.
class OperatorAccountInvalid(rest_exceptions.APIException):
    status_code = 400
    err_code = ErrorCodes.OPERATOR_ACCOUNT_INVALID


class PasswordInvalid(rest_exceptions.APIException):
    status_code = 400
    err_code = ErrorCodes.PASSWORD_INVALID


class InspectionNotFound(rest_exceptions.APIException):
    status_code = 400
    err_code = ErrorCodes.INSPECTION_NOT_FOUND


def giro_exception_handler(exc, context):
    """
    Returns the response that should be used for any given exception.
    """
    exception_response = default_exception_handler(exc, context)

    if not hasattr(exception_response, 'data'):
        return exception_response

    # Hide details from production users.
    if 'detail' in exception_response.data and not settings.DEBUG:
        del exception_response.data['detail']

    # Default to unknown error.
    error_code = ErrorCodes.UNKNOWN_ERROR

    if hasattr(exc, 'err_code'):
        error_code = exc.err_code
    elif exc.__class__ in ERR_CODE_MAP:
        error_code = ERR_CODE_MAP[exc.__class__]

    exception_response.data['code'] = error_code

    if hasattr(exc, 'status_code') and exc.status_code == 500:
        logging.error(
            'Internal Server Error was raised, logging a traceback...')
        logging.error(traceback.format_exc(exc))

    return exception_response
