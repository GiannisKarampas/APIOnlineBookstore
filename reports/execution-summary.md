# Online Bookstore API - Test Execution Report

Executed at 2026-09-24 13:04:56 +03:00 (Europe/Athens)

| Total | Passed | Flaky | Failed | Skipped | Pass rate | Elapsed | Cumulative |
|---|---|---|---|---|---|---|---|
| 123 | 123 | 0 | 0 | 0 | 100.0% | 9.7s | 30.2s |

Elapsed is wall-clock time; cumulative is the sum of test durations, which is larger because classes run in parallel.

| Status | Test case | Scenario | Attempts | Duration |
|---|---|---|---|---|
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.anUnsupportedMethodIsRefused` | A method the resource does not support is refused with 405 | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.authorIdsAreUnique` | Every author is listed under their own id | 1 | 261ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.everyAttributionIsWithinTheCatalogueRange` | The collection reports every author's book attribution within the catalogue range | 1 | 253ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.everyAuthorIsAttributedToAnExistingBook` | Every author is attributed to a book id that exists in the catalogue | 1 | 562ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.theAuthorListIsReturnedAsJson` | The author list is returned as a JSON collection of well-formed authors | 1 | 273ms |
| ✅ PASS | `TC_API_AUTHORS_01_GetAllAuthors.theAuthorListIsStableAcrossCalls` | Reading the list twice in a row returns a collection of the same shape | 1 | 508ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`[' ']` | 1 | 199ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['1.5']` | 1 | 186ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['2147483648']` | 1 | 191ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['abc']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['1']` | 1 | 186ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['100']` | 1 | 185ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['200']` | 1 | 182ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.aSeededAuthorIsReturned` | Each seeded author can be fetched by their id <br>`['50']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anExistingAuthorIsReturned` | An existing author is returned in full | 1 | 183ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['-1']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['0']` | 1 | 188ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['2147483647']` | 1 | 181ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnknownIdIsNotFound` | An id that matches no author is reported as not found <br>`['999999']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_02_GetAuthorById.anUnsupportedMethodIsRefused` | A method this resource does not support is refused with 405 | 1 | 185ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['id', '{"id":null,"idBook":1,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['idBook', '{"id":1,"idBook":null,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 190ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric book reference', '{"id":1,"idBook":"one","firstName":"Ada"}', '$.idBook']` | 1 | 197ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric id', '{"id":"abc","idBook":1,"firstName":"Ada"}', '$.id']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['an object where a first name belongs', '{"id":1,"idBook":1,"firstName":{}}', '$.firstName']` | 1 | 188ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.aValidAuthorIsAccepted` | A valid author is accepted and echoed back unchanged | 1 | 207ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anAuthorMayReferenceABookThatDoesNotExist` | Finding: an author may be attributed to a book that does not exist | 1 | 187ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anEmptyJsonObjectIsFilledWithDefaults` | An empty JSON object is accepted and filled with type defaults rather than rejected | 1 | 204ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 189ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['a bare string', '"just a string"']` | 1 | 186ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['an empty body', '']` | 1 | 187ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.anUnusableBodyIsRejected` | A body that is not an author object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.oversizedTextIsAcceptedWithoutTruncation` | Names far longer than the seeded data are accepted without truncation | 1 | 212ms |
| ✅ PASS | `TC_API_AUTHORS_03_CreateAuthor.theCreatedAuthorIsNotRetrievable` | An author accepted by POST cannot be read back, because the demo API keeps no state | 1 | 377ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when an update sends it as null <br>`['id', '{"id":null,"idBook":1,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when an update sends it as null <br>`['idBook', '{"id":1,"idBook":null,"firstName":"Ada","lastName":"Lovelace"}']` | 1 | 190ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUnsupportedMediaTypeIsRefused` | An update sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 202ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUnsupportedMediaTypeIsRefused` | An update sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 187ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUnusableBodyIsRejected` | An update whose body is not an author object is rejected <br>`['a bare string', '"just a string"']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUnusableBodyIsRejected` | An update whose body is not an author object is rejected <br>`['an empty body', '']` | 1 | 187ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUnusableBodyIsRejected` | An update whose body is not an author object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 181ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 185ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateToAnExistingAuthorIsAcknowledged` | An update to an existing author is acknowledged and echoed back | 1 | 181ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.anUpdateWithWrongFieldTypesIsRejected` | An update whose field types do not match the schema is rejected | 1 | 180ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.theBodyIdWinsOverThePathId` | When the id in the path and the id in the body disagree, the body wins | 1 | 180ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.theUpdateIsNotPersisted` | An update is not persisted, because the demo API keeps no state | 1 | 553ms |
| ✅ PASS | `TC_API_AUTHORS_04_UpdateAuthor.updatingAnUnknownAuthorIsAccepted` | Updating an id that matches no author is accepted rather than reported as not found | 1 | 189ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 184ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 188ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 187ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.aDeletionOfAnExistingAuthorIsAcknowledged` | A deletion of an existing author is acknowledged with an empty body | 1 | 186ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.deletingAnUnknownIdIsAccepted` | Deleting an id that matches no author is acknowledged rather than reported as not found | 1 | 193ms |
| ✅ PASS | `TC_API_AUTHORS_05_DeleteAuthor.theDeletionIsNotPersisted` | A deletion is not persisted, because the demo API keeps no state | 1 | 377ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.anUnsupportedMethodIsRefused` | A method the resource does not support is refused with 405 | 1 | 984ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.bookIdsAreUnique` | Every book in the catalogue is listed under its own id | 1 | 342ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.theCatalogueIsReturnedAsJson` | The catalogue is returned as a JSON collection of well-formed books | 1 | 538ms |
| ✅ PASS | `TC_API_BOOKS_01_GetAllBooks.theCatalogueIsStableAcrossCalls` | Reading the catalogue twice in a row returns the same books | 1 | 649ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`[' ']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['1.5']` | 1 | 190ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['2147483648']` | 1 | 191ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aNonIntegerIdIsRejected` | An id that is not an integer is rejected as a bad request <br>`['abc']` | 1 | 1001ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['1']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['100']` | 1 | 184ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['200']` | 1 | 192ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.aSeededBookIsReturned` | Each book of the seeded catalogue can be fetched by its id <br>`['50']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anExistingBookIsReturned` | An existing book is returned in full | 1 | 185ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['-1']` | 1 | 185ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['0']` | 1 | 181ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['201']` | 1 | 184ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['2147483647']` | 1 | 188ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnknownIdIsNotFound` | An id that matches no book is reported as not found <br>`['9999']` | 1 | 187ms |
| ✅ PASS | `TC_API_BOOKS_02_GetBookById.anUnsupportedMethodIsRefused` | A method this resource does not support is refused with 405 | 1 | 193ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNegativePageCountIsAccepted` | A negative page count is accepted, so the API applies no semantic validation | 1 | 1001ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNonUtcPublishDateKeepsItsInstant` | A publish date carrying a non-UTC offset keeps its instant | 1 | 189ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['id', '{"id":null,"title":"A title","description":"d","pageCount":1,"excerpt":"e","publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 194ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['pageCount', '{"id":1,"title":"A title","description":"d","pageCount":null,"excerpt":"e","publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when sent as null <br>`['publishDate', '{"id":1,"title":"A title","description":"d","pageCount":1,"excerpt":"e","publishDate":null}']` | 1 | 191ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric id', '{"id":"abc","title":"A title","pageCount":1}', '$.id']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['a non-numeric page count', '{"id":1,"title":"A title","pageCount":"many"}', '$.pageCount']` | 1 | 189ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aPayloadWithWrongFieldTypesIsRejected` | A payload whose field types do not match the schema is rejected <br>`['an unparsable publish date', '{"id":1,"publishDate":"not-a-date"}', '$.publishDate']` | 1 | 181ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.aValidBookIsAccepted` | A valid book is accepted and echoed back unchanged | 1 | 183ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anEmptyJsonObjectIsFilledWithDefaults` | Finding: an empty JSON object is filled with defaults, one of which is not a valid date-time | 1 | 408ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 180ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnsupportedMediaTypeIsRefused` | A body sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 187ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['a bare string', '"just a string"']` | 1 | 184ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['an empty body', '']` | 1 | 183ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.anUnusableBodyIsRejected` | A body that is not a book object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 178ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.awkwardTextSurvivesTheRoundTrip` | Valid but awkward text survives the round trip unchanged <br>`['CJK characters', '書籍のタイトル']` | 1 | 185ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.awkwardTextSurvivesTheRoundTrip` | Valid but awkward text survives the round trip unchanged <br>`['a newline and a tab', 'Line one
Line two	indented']` | 1 | 192ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.awkwardTextSurvivesTheRoundTrip` | Valid but awkward text survives the round trip unchanged <br>`['emoji outside the basic plane', 'A title 📚🔖']` | 1 | 193ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.awkwardTextSurvivesTheRoundTrip` | Valid but awkward text survives the round trip unchanged <br>`['quotes and a backslash', 'He said "hello" \ then left']` | 1 | 198ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.awkwardTextSurvivesTheRoundTrip` | Valid but awkward text survives the round trip unchanged <br>`['right-to-left text', 'عنوان الكتاب']` | 1 | 194ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.oversizedTextIsAcceptedWithoutTruncation` | Text fields far longer than the seeded data are accepted without truncation | 1 | 262ms |
| ✅ PASS | `TC_API_BOOKS_03_CreateBook.theCreatedBookIsNotRetrievable` | A book accepted by POST cannot be read back, because the demo API keeps no state | 1 | 387ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when an update sends it as null <br>`['id', '{"id":null,"title":"A title","pageCount":1,"publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 1001ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when an update sends it as null <br>`['pageCount', '{"id":1,"title":"A title","pageCount":null,"publishDate":"2026-01-01T00:00:00Z"}']` | 1 | 190ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.aNullNonNullableFieldIsRejected` | Each non-nullable field is rejected on its own when an update sends it as null <br>`['publishDate', '{"id":1,"title":"A title","pageCount":1,"publishDate":null}']` | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUnsupportedMediaTypeIsRefused` | An update sent under a media type the API does not accept is refused <br>`['XML', 'application/xml']` | 1 | 202ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUnsupportedMediaTypeIsRefused` | An update sent under a media type the API does not accept is refused <br>`['plain text', 'text/plain']` | 1 | 187ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUnusableBodyIsRejected` | An update whose body is not a book object is rejected <br>`['a bare string', '"just a string"']` | 1 | 191ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUnusableBodyIsRejected` | An update whose body is not a book object is rejected <br>`['an empty body', '']` | 1 | 184ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUnusableBodyIsRejected` | An update whose body is not a book object is rejected <br>`['truncated JSON', '{"id":']` | 1 | 181ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 192ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 189ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToANonIntegerIdIsRejected` | An update addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 183ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateToAnExistingBookIsAcknowledged` | An update to an existing book is acknowledged and echoed back | 1 | 187ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.anUpdateWithWrongFieldTypesIsRejected` | An update whose field types do not match the schema is rejected | 1 | 195ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.theBodyIdWinsOverThePathId` | When the id in the path and the id in the body disagree, the body wins | 1 | 184ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.theUpdateIsNotPersisted` | An update is not persisted, because the demo API keeps no state | 1 | 565ms |
| ✅ PASS | `TC_API_BOOKS_04_UpdateBook.updatingAnUnknownIdIsAccepted` | Updating an id that matches no book is accepted rather than reported as not found | 1 | 192ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['1.5']` | 1 | 197ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['2147483648']` | 1 | 183ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfANonIntegerIdIsRejected` | A deletion addressed to a non-integer id is rejected as a bad request <br>`['abc']` | 1 | 186ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.aDeletionOfAnExistingBookIsAcknowledged` | A deletion of an existing book is acknowledged with an empty body | 1 | 183ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.deletingAnUnknownIdIsAccepted` | Deleting an id that matches no book is acknowledged rather than reported as not found | 1 | 185ms |
| ✅ PASS | `TC_API_BOOKS_05_DeleteBook.theDeletionIsNotPersisted` | A deletion is not persisted, because the demo API keeps no state | 1 | 368ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.aDocumentedOperationIsCheckedAgainstItsSchema` | A documented operation is checked against its schema, not merely its status code | 1 | 204ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theContractDeclaresNoBodyOnAWrite` | Finding: the contract declares no body on a write, although the API returns one | 1 | 190ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theContractDocumentsNoErrorResponses` | Finding: the contract documents no error responses, although the API returns them | 1 | 191ms |
| ✅ PASS | `TC_API_CONTRACT_01_OpenApiContract.theSnapshotMatchesThePublishedDocument` | The committed snapshot still matches the published document where this suite relies on it | 1 | 263ms |
