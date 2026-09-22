# Online Bookstore API - Test Execution Report

Executed at 2026-09-22 15:04:47

| Total | Passed | Flaky | Failed | Skipped | Pass rate | Elapsed | Cumulative |
|---|---|---|---|---|---|---|---|
| 99 | 99 | 0 | 0 | 0 | 100.0% | 10.7s | 32.6s |

Elapsed is wall-clock time; cumulative is the sum of test durations, which is larger because classes run in parallel.

| Status | Test case | Scenario | Attempts | Duration |
|---|---|---|---|---|
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.anUnsupportedMethodIsRefused` | A method the resource does not support is refused with 405 | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.authorIdsAreUnique` | Every author is listed under their own id | 1 | 327ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.everyAttributionIsWithinTheCatalogueRange` | The collection reports every author's book attribution within the catalogue range | 1 | 325ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.everyAuthorIsAttributedToAnExistingBook` | Every author is attributed to a book id that exists in the catalogue | 1 | 712ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.theAuthorListIsReturnedAsJson` | The author list is returned as a JSON collection of well-formed authors | 1 | 351ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.theAuthorListIsStableAcrossCalls` | Reading the list twice in a row returns a collection of the same shape | 1 | 651ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`[' ']` | 1 | 236ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['1.5']` | 1 | 246ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['2147483648']` | 1 | 236ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['abc']` | 1 | 239ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['1']` | 1 | 242ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['100']` | 1 | 235ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['200']` | 1 | 245ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['50']` | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anExistingAuthorIsReturned` | An existing author is returned in full | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['-1']` | 1 | 237ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['0']` | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['2147483647']` | 1 | 240ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['999999']` | 1 | 236ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['id', '{"id":null,"idBook":1,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 242ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['idBook', '{"id":1,"idBook":null,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 239ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric book reference', '{"id":1,"idBook":"one","firstName":"Ada"}', '$.idBook']` | 1 | 241ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric id', '{"id":"abc","idBook":1,"firstName":"Ada"}', '$.id']` | 1 | 240ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a numeric first name', '{"id":1,"idBook":1,"firstName":{}}', '$.firstName']` | 1 | 248ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aValidAuthorIsAccepted` | A valid author is accepted and echoed back unchanged | 1 | 244ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anAuthorMayReferenceABookThatDoesNotExist` | Finding: an author may be attributed to a book that does not exist | 1 | 241ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anEmptyJsonObjectIsFilledWithDefaults` | An empty JSON object is accepted and filled with type defaults rather than rejected | 1 | 258ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 241ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['a bare string', '"just a string"']` | 1 | 234ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['an empty body', '']` | 1 | 236ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 235ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.oversizedTextIsAcceptedWithoutTruncation` | Names far longer than the seeded data are accepted without truncation | 1 | 245ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.theCreatedAuthorIsNotRetrievable` | An author accepted by POST cannot be read back, because the demo API keeps no state | 1 | 481ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anExistingAuthorIsUpdated` | An existing author is updated and the new version is echoed back | 1 | 265ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 240ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 235ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateWithWrongFieldTypesIsRejected` | An update whose field types do not match the schema is rejected | 1 | 232ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.theBodyIdWinsOverThePathId` | When the id in the path and the id in the body disagree, the body wins | 1 | 243ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.theUpdateIsNotPersisted` | An update is not persisted, because the demo API keeps no state | 1 | 735ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.updatingAnUnknownAuthorIsAccepted` | Updating an id that matches no author is accepted rather than reported as not found | 1 | 239ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 239ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 236ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 238ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.anExistingAuthorIsDeleted` | Deleting an existing author is acknowledged with an empty body | 1 | 241ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.deletingAnUnknownIdIsAccepted` | Deleting an id that matches no author is acknowledged rather than reported as not found | 1 | 244ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.theDeletionIsNotPersisted` | A deletion is not persisted, because the demo API keeps no state | 1 | 493ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.anUnsupportedMethodIsRefused` | A method the resource does not support is refused with 405 | 1 | 1148ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.bookIdsAreUnique` | Every book in the catalogue is listed under its own id | 1 | 424ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.theCatalogueIsReturnedAsJson` | The catalogue is returned as a JSON collection of well-formed books | 1 | 740ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.theCatalogueIsStableAcrossCalls` | Reading the catalogue twice in a row returns the same books | 1 | 798ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`[' ']` | 1 | 259ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['1.5']` | 1 | 242ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['2147483648']` | 1 | 247ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['abc']` | 1 | 1169ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['1']` | 1 | 267ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['100']` | 1 | 245ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['200']` | 1 | 250ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['50']` | 1 | 247ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anExistingBookIsReturned` | An existing book is returned in full | 1 | 248ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['-1']` | 1 | 241ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['0']` | 1 | 233ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['2147483647']` | 1 | 247ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['9999']` | 1 | 240ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNegativePageCountIsAccepted` | A negative page count is accepted, so the API applies no semantic validation | 1 | 1182ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['id', '{"id":null,"title":"A title","description":"d","pageCount":1,"excerpt":"e","publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 241ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['pageCount', '{"id":1,"title":"A title","description":"d","pageCount":null,"excerpt":"e","publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 248ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['publishDate', '{"id":1,"title":"A title","description":"d","pageCount":1,"excerpt":"e","publishDate":null}']` | 1 | 251ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric id', '{"id":"abc","title":"A title","pageCount":1}', '$.id']` | 1 | 248ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric page count', '{"id":1,"title":"A title","pageCount":"many"}', '$.pageCount']` | 1 | 251ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['an unparsable publish date', '{"id":1,"publishDate":"not-a-date"}', '$.publishDate']` | 1 | 276ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aValidBookIsAccepted` | A valid book is accepted and echoed back unchanged | 1 | 236ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anEmptyJsonObjectIsFilledWithDefaults` | Finding: an empty JSON object is filled with defaults, one of which is not a valid date-time | 1 | 465ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 247ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 237ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['a bare string', '"just a string"']` | 1 | 241ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['an empty body', '']` | 1 | 249ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 240ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.oversizedTextIsAcceptedWithoutTruncation` | Text fields far longer than the seeded data are accepted without truncation | 1 | 326ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.theCreatedBookIsNotRetrievable` | A book accepted by POST cannot be read back, because the demo API keeps no state | 1 | 500ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anExistingBookIsUpdated` | An existing book is updated and the new version is echoed back | 1 | 1168ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 245ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 249ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 246ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateWithWrongFieldTypesIsRejected` | An update whose field types do not match the schema is rejected | 1 | 266ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.theBodyIdWinsOverThePathId` | When the id in the path and the id in the body disagree, the body wins | 1 | 246ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.theUpdateIsNotPersisted` | An update is not persisted, because the demo API keeps no state | 1 | 731ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.updatingAnUnknownIdIsAccepted` | Updating an id that matches no book is accepted rather than reported as not found | 1 | 242ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 232ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 259ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 246ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.anExistingBookIsDeleted` | Deleting an existing book is acknowledged with an empty body | 1 | 242ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.deletingAnUnknownIdIsAccepted` | Deleting an id that matches no book is acknowledged rather than reported as not found | 1 | 236ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.theDeletionIsNotPersisted` | A deletion is not persisted, because the demo API keeps no state | 1 | 491ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.aDocumentedOperationIsCheckedAgainstItsSchema` | A documented operation is checked against its schema, not merely its status code | 1 | 252ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theContractDeclaresNoBodyOnAWrite` | Finding: the contract declares no body on a write, although the API returns one | 1 | 253ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theContractDocumentsNoErrorResponses` | Finding: the contract documents no error responses, although the API returns them | 1 | 244ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theSnapshotMatchesThePublishedDocument` | The committed snapshot still matches the published document where this suite relies on it | 1 | 335ms |
